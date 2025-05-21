// src/main/java/com/sxt/service/impl/DoctorRecommendServiceImpl.java
package com.sxt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sxt.mapper.ExpertiseMapper;
import com.sxt.mapper.DoctorRecommendMapper;
import com.sxt.mapper.DoctorReviewMapper;
import com.sxt.mapper.SymptomMapper;
import com.sxt.mapper.UserMapper;
import com.sxt.pojo.DoctorExpertise;
import com.sxt.pojo.Expertise;
import com.sxt.pojo.RecommendedDoctorDTO;
import com.sxt.pojo.DoctorReview;
import com.sxt.pojo.User;
import com.sxt.service.DoctorRecommendService;
import com.sxt.service.ZhipuAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sxt.config.ZhipuAiConfig;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * 医生推荐服务实现类 - 使用混合推荐算法
 */
@Service
@Slf4j
public class DoctorRecommendServiceImpl implements DoctorRecommendService {
    
    private static final float MIN_RATING = 1.0f;
    private static final float MAX_RATING = 5.0f;
    
    // 智谱API URL常量
    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    
    // 推荐权重配置
    private static final float EXPERTISE_WEIGHT = 0.6f;    // 专长匹配权重
    private static final float RATING_WEIGHT = 0.2f;       // 评分权重
    private static final float COLLABORATIVE_WEIGHT = 0.2f; // 协同过滤权重
    
    @Autowired
    private DoctorRecommendMapper doctorRecommendMapper;
    
    @Autowired
    private DoctorReviewMapper doctorReviewMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ZhipuAiService zhipuAiService;

    @Autowired
    private ExpertiseMapper expertiseMapper;

    @Autowired
    private ZhipuAiConfig zhipuAiConfig;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<RecommendedDoctorDTO> recommendDoctorsByNaturalLanguage(String naturalLanguageDescription, Integer userId, int limit) {
        if (naturalLanguageDescription == null || naturalLanguageDescription.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        log.info("处理自然语言输入: {}", naturalLanguageDescription);
        
        try {
            // 使用不依赖AI的关键词提取方法
            List<String> extractedKeywords = extractKeywordsWithoutAI(naturalLanguageDescription);
            if (extractedKeywords.isEmpty()) {
                log.warn("无法从输入中提取关键词");
                return Collections.emptyList();
            }
            
            log.info("提取的关键词: {}", extractedKeywords);
            
            // 2. 根据关键词匹配症状并获取相关专长
            List<Map<String, Object>> matchedExpertise = doctorRecommendMapper.matchSymptomsByKeywords(extractedKeywords);
            
            // 即使匹配结果很少或相关性较低，也继续处理
            if (matchedExpertise.isEmpty()) {
                log.warn("无法找到匹配的症状和专长，尝试使用更宽泛的匹配策略");
                // 可以在这里添加备选匹配策略，例如使用更通用的关键词
                // 但即使没有匹配，也继续执行，后续会返回评分最高的医生
            }
            
            // 3. 获取相关专长ID - 即使列表为空也继续处理
            List<Integer> expertiseIds = matchedExpertise.stream()
                .map(map -> (Integer) map.get("expertise_id"))
                .distinct()
                .collect(Collectors.toList());
            
            // 4. 获取具有这些专长的医生
            List<RecommendedDoctorDTO> doctors = new ArrayList<>();
            
            if (!expertiseIds.isEmpty()) {
                doctors = doctorRecommendMapper.getDoctorsByExpertises(expertiseIds);
            }
            
            // 如果没有找到具有特定专长的医生，获取所有医生
            if (doctors.isEmpty()) {
                log.warn("没有找到具有相关专长的医生，获取所有医生并计算相关性");
                doctors = doctorRecommendMapper.getAllDoctors(Math.max(limit * 2, 20)); // 获取更多医生
            }
            
            // 5. 获取协同过滤推荐（如果用户已登录）
            Map<Integer, Float> collaborativeScores = new HashMap<>();
            if (userId != null) {
                collaborativeScores = getUserBasedRecommendations(userId, limit);
            }
            
            // 6. 计算最终推荐分数
            for (RecommendedDoctorDTO doctor : doctors) {
                float expertiseScore = 0.0f; // 设置基础分数
                
                // 只有在有匹配结果时才计算专长分数
                if (!matchedExpertise.isEmpty()) {
                    expertiseScore = Math.max(calculateExpertiseScore(doctor, matchedExpertise), 0.0f);
                }
                
                float ratingScore = calculateRatingScore(doctor);
                float collaborativeScore = collaborativeScores.getOrDefault(doctor.getDoctorId(), 0f);
                
                log.info("计算医生 {} 的最终得分:", doctor.getName());
                log.info("- 专长匹配得分: {} (权重: {})", String.format("%.2f", expertiseScore), EXPERTISE_WEIGHT);
                log.info("- 评价得分: {} (权重: {})", String.format("%.2f", ratingScore), RATING_WEIGHT);
                log.info("- 协同过滤得分: {} (权重: {})", String.format("%.2f", collaborativeScore), COLLABORATIVE_WEIGHT);
                
                // 使用与症状ID模式相同的权重配置
                float finalScore = Math.round((expertiseScore * EXPERTISE_WEIGHT + 
                                 ratingScore * RATING_WEIGHT + 
                                 collaborativeScore * COLLABORATIVE_WEIGHT) * 100) / 100.0f;
                
                log.info("医生 {} 的最终得分: {}", doctor.getName(), String.format("%.2f", finalScore));
                
                doctor.setMatchScore(finalScore);
                setRecommendReason(doctor, expertiseScore, ratingScore, collaborativeScore);
            }
            
            // 7. 按匹配度排序但返回更多结果
            List<RecommendedDoctorDTO> sortedDoctors = doctors.stream()
                .sorted(Comparator.comparing(RecommendedDoctorDTO::getMatchScore).reversed())
                .collect(Collectors.toList());
                
            // 返回的数量可以更大，确保包括一些匹配度较低的医生
            int maxResults = Math.min(sortedDoctors.size(), Math.max(limit * 2, 20));
            return sortedDoctors.subList(0, maxResults);
                
        } catch (Exception e) {
            log.error("自然语言推荐失败", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 不使用AI服务提取关键词
     * 极简版本：只进行基本分词，然后与数据库匹配
     */
    private List<String> extractKeywordsWithoutAI(String description) {
        log.info("使用极简本地方法提取关键词: {}", description);
        
        // 如果描述为空，返回空列表
        if (description == null || description.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // 简单过滤一些常见无关词
        String[] stopWords = {"我", "的", "了", "是", "在", "有", "和", "就", "不", "也", "都", "还有"};
        
        // 1. 清理文本，去除标点符号
        String cleanText = description.replaceAll("[,，。、;；!！?？\\s]", " ");
        
        // 2. 简单分词（按空格分割）
        String[] words = cleanText.split(" ");
        
        // 3. 过滤并收集关键词
        List<String> keywords = new ArrayList<>();
        for (String word : words) {
            word = word.trim();
            // 只保留长度至少为2且不是停用词的词
            if (word.length() >= 2) {
                boolean isStopWord = false;
                for (String stopWord : stopWords) {
                    if (stopWord.equals(word)) {
                        isStopWord = true;
                        break;
                    }
                }
                if (!isStopWord) {
                    keywords.add(word);
                }
            }
        }
        
        // 4. 如果分词后没有有效关键词，尝试按字符拆分
        if (keywords.isEmpty()) {
            // 清理后的文本去除所有空格
            String noSpaceText = cleanText.replaceAll("\\s+", "");
            
            // 按每2个字符进行分组
            for (int i = 0; i < noSpaceText.length() - 1; i++) {
                String twoChars = noSpaceText.substring(i, Math.min(i + 2, noSpaceText.length()));
                keywords.add(twoChars);
                // 为避免关键词过多，最多添加5个
                if (keywords.size() >= 5) {
                    break;
                }
            }
        }
        
        log.info("分词提取的关键词: {}", keywords);
        return keywords;
    }
    
    @Override
    public List<RecommendedDoctorDTO> findTopRatedDoctors(int limit) {
        log.info("获取评分最高的 {} 位医生", limit);
        
        try {
            List<RecommendedDoctorDTO> topRatedDoctors = doctorRecommendMapper.getTopRatedDoctors(limit);
            
            if (topRatedDoctors.isEmpty()) {
                log.info("无法获取评分最高的医生，尝试获取任意医生");
                topRatedDoctors = doctorRecommendMapper.getAllDoctors(limit);
            }
            
            if (topRatedDoctors.isEmpty()) {
                log.warn("无法获取任何医生数据");
                return Collections.emptyList();
            }
            
            // 设置推荐原因和匹配分数
            for (RecommendedDoctorDTO doctor : topRatedDoctors) {
                float ratingScore = calculateRatingScore(doctor);
                doctor.setMatchScore(ratingScore);
                
                if (ratingScore > 0.7f) {
                    doctor.setRecommendReason("患者评价极高");
                } else if (ratingScore > 0.5f) {
                    doctor.setRecommendReason("患者评价良好");
                } else {
                    doctor.setRecommendReason("医院推荐医生");
                }
            }
            
            return topRatedDoctors;
            
        } catch (Exception e) {
            log.error("获取评分最高的医生失败", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<RecommendedDoctorDTO> recommendDoctorsByUserProfile(Integer userId, int limit) {
        log.info("基于用户画像(ID: {})推荐医生", userId);
        
        try {
            User user = userMapper.selectById(userId.longValue());
            if (user == null || user.getMedicalRecord() == null || user.getMedicalRecord().trim().isEmpty()) {
                log.warn("用户(ID: {})没有病历记录，返回评分最高的医生", userId);
                return findTopRatedDoctors(limit);
            }
            
            log.info("使用用户病历记录进行推荐：{}", user.getMedicalRecord());
            return recommendDoctorsByNaturalLanguage(user.getMedicalRecord(), userId, limit);
            
        } catch (Exception e) {
            log.error("基于用户画像推荐医生失败", e);
            return findTopRatedDoctors(limit);
        }
    }
    
    @Override
    public List<RecommendedDoctorDTO> aiDirectRecommendation(String symptomDescription, Integer userId, int limit) {
        if (symptomDescription == null || symptomDescription.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        log.info("使用AI直接推荐 - 症状描述: {}", symptomDescription);
        
        try {
            // 1. 使用智谱AI分析症状并推荐专长
            List<String> recommendedExpertises = zhipuAiService.analyzeSymptoms(symptomDescription);
            if (recommendedExpertises.isEmpty()) {
                log.warn("AI无法从症状中识别相关专长");
                return recommendDoctorsByNaturalLanguage(symptomDescription, userId, limit);
            }
            
            log.info("AI推荐的专长: {}", recommendedExpertises);
            
            // 2. 获取具有这些专长的医生
            List<Integer> expertiseIds = recommendedExpertises.stream()
                .map(expertise -> expertiseMapper.getExpertiseIdByName(expertise))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
                
            if (expertiseIds.isEmpty()) {
                log.warn("没有找到匹配的专长ID");
                return recommendDoctorsByNaturalLanguage(symptomDescription, userId, limit);
            }
            
            List<RecommendedDoctorDTO> doctors = doctorRecommendMapper.getDoctorsByExpertises(expertiseIds);
            
            if (doctors.isEmpty()) {
                log.warn("没有找到具有AI推荐专长的医生");
                return recommendDoctorsByNaturalLanguage(symptomDescription, userId, limit);
            }
            
            // 3. 结合协同过滤进行个性化推荐
            Map<Integer, Float> collaborativeScores = new HashMap<>();
            if (userId != null) {
                collaborativeScores = getUserBasedRecommendations(userId, limit);
            }
            
            // 4. 计算最终得分
            for (RecommendedDoctorDTO doctor : doctors) {
                float aiMatchScore = 0.8f; // AI推荐的专长匹配度较高
                float ratingScore = calculateRatingScore(doctor);
                float collaborativeScore = collaborativeScores.getOrDefault(doctor.getDoctorId(), 0f);
                
                float finalScore = Math.round((aiMatchScore * 0.6f + 
                                 ratingScore * 0.2f + 
                                 collaborativeScore * 0.2f) * 100) / 100.0f;
                
                doctor.setMatchScore(finalScore);
                
                StringBuilder reason = new StringBuilder("AI推荐：专长与您的症状相符");
                if (ratingScore > 0.5f) reason.append("，患者评价良好");
                if (collaborativeScore > 0.5f) reason.append("，类似患者反馈积极");
                doctor.setRecommendReason(reason.toString());
            }
            
            return doctors.stream()
                .sorted(Comparator.comparing(RecommendedDoctorDTO::getMatchScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("AI直接推荐失败", e);
            return recommendDoctorsByNaturalLanguage(symptomDescription, userId, limit);
        }
    }
    
    /**
     * 计算专长匹配度分数
     */
    private float calculateExpertiseScore(RecommendedDoctorDTO doctor, List<Map<String, Object>> matchedExpertise) {
        log.info("开始计算医生 {} 的专长匹配度分数", doctor.getName());
        
        if (doctor.getExpertiseList() == null || doctor.getExpertiseList().isEmpty()) {
            log.info("医生 {} 没有专长信息，返回基础分数 0.0", doctor.getName());
            return 0.0f;
        }
        
        float totalScore = 0f;
        float totalWeight = 0f;
        boolean hasAnyMatch = false;
        
        log.info("医生 {} 的专长列表: {}", doctor.getName(), doctor.getExpertiseList());
        log.info("需要匹配的专长数量: {}", matchedExpertise.size());
        
        // 解析专长信息
        String[] expertisePairs = doctor.getExpertiseList().split(";");
        for (String expertisePair : expertisePairs) {
            try {
                // 首先尝试提取专长名称和熟练度
                final String finalExpertiseName;  // 使用final变量
                float proficiency = 0.0f; // 默认熟练度

                // 检查是否包含":"
                if (expertisePair.contains(":")) {
                    String[] parts = expertisePair.split(":");
                    // 如果格式是 "id:name:proficiency"
                    if (parts.length >= 3) {
                        finalExpertiseName = parts[1].trim();
                        String profStr = parts[2].trim().replace("%", "");
                        proficiency = Float.parseFloat(profStr) / 100.0f;
                    }
                    // 如果格式是 "name:proficiency"
                    else if (parts.length == 2) {
                        finalExpertiseName = parts[0].trim();
                        String profStr = parts[1].trim().replace("%", "");
                        proficiency = Float.parseFloat(profStr) / 100.0f;
                    }
                    else {
                        finalExpertiseName = expertisePair.trim();
                    }
                } else {
                    // 如果没有":"，就把整个字符串作为专长名称
                    finalExpertiseName = expertisePair.trim();
                }

                if (finalExpertiseName == null) {
                    log.warn("医生 {} 的专长信息格式不正确: {}", doctor.getName(), expertisePair);
                    continue;
                }
                
                log.debug("处理医生专长: {}, 熟练度: {}", finalExpertiseName, proficiency);
                
                // 查找匹配的专长
                Optional<Map<String, Object>> matchedEntry = matchedExpertise.stream()
                    .filter(e -> finalExpertiseName.equals(e.get("expertise_name")))
                    .findFirst();
                    
                if (matchedEntry.isPresent()) {
                    hasAnyMatch = true;
                    Object relevanceObj = matchedEntry.get().getOrDefault("relevance_score", 0.0f);
                    Object keywordMatchObj = matchedEntry.get().getOrDefault("keyword_match_score", 0.0f);
                    
                    float relevanceScore = relevanceObj instanceof Number ? 
                        ((Number) relevanceObj).floatValue() : 
                        Float.parseFloat(relevanceObj.toString());
                    float keywordMatchScore = keywordMatchObj instanceof Number ? 
                        ((Number) keywordMatchObj).floatValue() : 
                        Float.parseFloat(keywordMatchObj.toString());
                    
                    float combinedScore = (float) (Math.round(keywordMatchScore * relevanceScore * proficiency * 100.0) / 100.0);
                    
                    log.info("找到专长匹配 - 专长: {}, 相关度: {}, 关键词匹配度: {}, 熟练度: {}, 组合分数: {}", 
                        finalExpertiseName, 
                        String.format("%.2f", relevanceScore), 
                        String.format("%.2f", keywordMatchScore), 
                        String.format("%.2f", proficiency), 
                        String.format("%.2f", combinedScore));
                    
                    totalScore += combinedScore;
                    totalWeight += 1.0f;
                }
            } catch (Exception e) {
                log.warn("解析医生 {} 的专长信息时出错: {} - {}", doctor.getName(), expertisePair, e.getMessage());
                continue;
            }
        }
        
        if (!hasAnyMatch || totalWeight == 0) {
            log.info("医生 {} 没有找到任何匹配的专长，返回基础分数0", doctor.getName());
            return 0.0f;
        }
        
        float finalScore = (float) (Math.round(totalScore / totalWeight * 100.0) / 100.0);
        log.info("医生 {} 的最终专长匹配度分数: {} (总分: {}, 总权重: {})", 
            doctor.getName(), 
            String.format("%.2f", finalScore),
            String.format("%.2f", totalScore),
            String.format("%.2f", totalWeight));
        
        return finalScore;
    }
    
    /**
     * 计算评分权重分数
     */
    @Override
    public float calculateRatingScore(RecommendedDoctorDTO doctor) {
        log.info("开始计算医生 {} 的评分权重", doctor.getName());
        
        if (doctor.getRatingCount() == null || doctor.getRatingCount() == 0 || doctor.getAverageRating() == null) {
            log.info("医生 {} 没有评分记录，返回默认分数0", doctor.getName());
            return 0f;
        }
        
        // 计算归一化评分
        float normalizedScore = (doctor.getAverageRating() - MIN_RATING) / (MAX_RATING - MIN_RATING);
        log.info("医生 {} 的原始评分: {}, 评价人数: {}, 归一化后评分: {}", 
            doctor.getName(), 
            doctor.getAverageRating(),
            doctor.getRatingCount(),
            String.format("%.2f", normalizedScore));
            
        return normalizedScore;
    }
    
    /**
     * 设置推荐原因
     * 根据专长匹配度、评分情况、协同过滤分数生成个性化推荐理由
     */
    private void setRecommendReason(RecommendedDoctorDTO doctor, float expertiseScore, 
                                  float ratingScore, float collaborativeScore) {
        StringBuilder reason = new StringBuilder();
        
        // 1. 专长匹配度说明
        if (expertiseScore > 0.8f) {
            reason.append("该医生的专业特长与您的症状高度匹配");
            if (doctor.getExpertiseList() != null) {
                String[] expertiseParts = doctor.getExpertiseList().split(";")[0].split(":");
                if (expertiseParts.length >= 3) {
                    String expertiseName = expertiseParts[1];
                    String proficiency = String.format("%.0f%%", Float.parseFloat(expertiseParts[2]) * 100);
                    reason.append("，特别擅长").append(expertiseName).append(":").append(proficiency);
                }
            }
            reason.append("。");
        } else if (expertiseScore > 0.6f) {
            reason.append("医生的专业特长与您的症状匹配度较高。");
        } else if (expertiseScore > 0.4f) {
            reason.append("医生具备相关专业特长。");
        } else if (expertiseScore > 0.2f) {
            reason.append("医生的专业方向可能与您的症状有一定关联。");
        } else {
            // 对于匹配度很低的医生，强调其他优势
            if (doctor.getPositionsName() != null && !doctor.getPositionsName().isEmpty()) {
                reason.append("该医生是").append(doctor.getPositionsName()).append("，");
            }
            if (doctor.getDepartmentName() != null && !doctor.getDepartmentName().isEmpty()) {
                reason.append("在").append(doctor.getDepartmentName()).append("工作，");
            }
            if (doctor.getExpertiseList() != null && !doctor.getExpertiseList().isEmpty()) {
                String[] expertiseParts = doctor.getExpertiseList().split(";")[0].split(":");
                if (expertiseParts.length >= 3) {
                    String expertiseName = expertiseParts[1];
                    String proficiency = String.format("%.0f%%", Float.parseFloat(expertiseParts[2]) * 100);
                    reason.append("擅长").append(expertiseName).append(":").append(proficiency).append("。");
                }
            } else {
                reason.append("可能对您的情况有所帮助。");
            }
        }
        
        // 2. 患者评价说明
        if (doctor.getRatingCount() != null && doctor.getRatingCount() > 0) {
            if (ratingScore > 0.8f) {
                reason.append("深受患者好评，");
                if (doctor.getRatingCount() > 100) {
                    reason.append("已有").append(doctor.getRatingCount()).append("位患者给出好评。");
                } else {
                    reason.append("患者评价极高。");
                }
            } else if (ratingScore > 0.6f) {
                reason.append("患者评价良好，满意度较高。");
            } else if (ratingScore > 0.4f) {
                reason.append("获得患者认可。");
            } else if (ratingScore > 0) {
                // 添加对评分较低医生的处理
                reason.append("有").append(doctor.getRatingCount()).append("位患者对其进行了评价。");
            }
        }
        
        // 3. 协同过滤推荐说明
        if (collaborativeScore > 0.8f) {
            reason.append("与您情况相似的患者对该医生评价非常正面。");
        } else if (collaborativeScore > 0.6f) {
            reason.append("多位相似患者对该医生反馈良好。");
        } else if (collaborativeScore > 0.4f) {
            reason.append("其他相似患者也选择过该医生。");
        } else if (collaborativeScore > 0.2f) {
            reason.append("有类似症状的患者曾就诊于此医生。");
        }
        
        // 4. 职称和科室信息（如果前面没有提到）
        if (reason.indexOf(doctor.getPositionsName() != null ? doctor.getPositionsName() : "") == -1 &&
            doctor.getPositionsName() != null && !doctor.getPositionsName().isEmpty()) {
            reason.append("医生目前担任").append(doctor.getPositionsName());
            if (doctor.getDepartmentName() != null && !doctor.getDepartmentName().isEmpty() &&
                reason.indexOf(doctor.getDepartmentName()) == -1) {
                reason.append("，在").append(doctor.getDepartmentName()).append("工作");
            }
            reason.append("。");
        }
        
        // 5. 匹配度较低时添加提示
        if (expertiseScore <= 0.3f && ratingScore <= 0.3f && collaborativeScore <= 0.2f) {
            if (doctor.getWorkYears() != null && doctor.getWorkYears() > 5) {
                reason.append("有").append(doctor.getWorkYears()).append("年临床经验。");
            }
            // 添加匹配度提示
            reason.append("(此推荐匹配度较低，供参考)");
        }
        
        doctor.setRecommendReason(reason.toString().trim());
    }
    
    /**
     * 获取基于用户的协同过滤推荐分数
     */
    @Override
    @Cacheable(value = "userRecommendations", key = "#userId")
    public Map<Integer, Float> getUserBasedRecommendations(Integer userId, int limit) {
        log.info("开始为用户 {} 计算协同过滤推荐分数", userId);
        
        List<DoctorReview> userReviews = doctorReviewMapper.getUserRatings(userId);
        log.info("用户 {} 有 {} 条评价记录", userId, userReviews.size());
        
        if (userReviews.isEmpty()) {
            log.info("用户 {} 没有评价记录，无法进行协同过滤", userId);
            return Collections.emptyMap();
        }

        Set<Integer> doctorIds = userReviews.stream()
            .map(DoctorReview::getDoctorId)
            .collect(Collectors.toSet());
        log.info("用户 {} 评价过的医生ID: {}", userId, doctorIds);
            
        List<DoctorReview> allReviews = new ArrayList<>();
        for (Integer doctorId : doctorIds) {
            List<DoctorReview> doctorReviews = doctorReviewMapper.getDoctorRatings(doctorId);
            log.info("医生 {} 有 {} 条评价记录", doctorId, doctorReviews.size());
            allReviews.addAll(doctorReviews);
        }
        log.info("总共收集到 {} 条相关评价记录", allReviews.size());
        
        Map<Integer, Float> similarUsers = new HashMap<>();
        Set<Long> userIds = allReviews.stream()
            .map(DoctorReview::getUserId)
            .collect(Collectors.toSet());
        log.info("找到 {} 个相关用户", userIds.size());
            
        for (Long otherUserId : userIds) {
            if (!otherUserId.equals(userId.longValue())) {
                float similarity = calculateUserSimilarity(userId, otherUserId.intValue());
                log.info("用户 {} 与用户 {} 的相似度: {}", userId, otherUserId, similarity);
                if (similarity > 0) {
                    similarUsers.put(otherUserId.intValue(), similarity);
                }
            }
        }
        log.info("找到 {} 个相似用户", similarUsers.size());

        Map<Integer, Float> recommendationScores = new HashMap<>();
        Map<Integer, Float> similaritySums = new HashMap<>();

        for (DoctorReview review : allReviews) {
            Integer otherUserId = review.getUserId().intValue();
            if (similarUsers.containsKey(otherUserId)) {
                float similarity = similarUsers.get(otherUserId);
                Integer doctorId = review.getDoctorId();
                // 将评分归一化到0-1范围
                float normalizedRating = (review.getRating() - MIN_RATING) / (MAX_RATING - MIN_RATING);
                recommendationScores.merge(doctorId, similarity * normalizedRating, Float::sum);
                similaritySums.merge(doctorId, similarity, Float::sum);
            }
        }

        Map<Integer, Float> normalizedScores = new HashMap<>();
        for (Map.Entry<Integer, Float> entry : recommendationScores.entrySet()) {
            Integer doctorId = entry.getKey();
            float similaritySum = similaritySums.get(doctorId);
            if (similaritySum > 0) {
                float normalizedScore = entry.getValue() / similaritySum;
                normalizedScores.put(doctorId, normalizedScore);
                log.info("医生 {} 的协同过滤推荐分数: {}", doctorId, normalizedScore);
            }
        }
        
        log.info("协同过滤计算完成，为 {} 个医生生成了推荐分数", normalizedScores.size());
        return normalizedScores;
    }
    
    /**
     * 计算用户相似度（使用余弦相似度）
     */
    private float calculateUserSimilarity(Integer userId1, Integer userId2) {
        log.info("计算用户 {} 和用户 {} 的相似度", userId1, userId2);
        
        List<DoctorReview> reviews1 = doctorReviewMapper.getUserRatings(userId1);
        List<DoctorReview> reviews2 = doctorReviewMapper.getUserRatings(userId2);
        log.info("用户 {} 有 {} 条评价，用户 {} 有 {} 条评价", 
                userId1, reviews1.size(), userId2, reviews2.size());

        Map<Integer, Float> ratings1 = reviews1.stream()
            .collect(Collectors.toMap(
                DoctorReview::getDoctorId,
                review -> review.getRating().floatValue(),
                (existing, replacement) -> (existing + replacement) / 2  // 当有重复评分时取平均值
            ));
        Map<Integer, Float> ratings2 = reviews2.stream()
            .collect(Collectors.toMap(
                DoctorReview::getDoctorId,
                review -> review.getRating().floatValue(),
                (existing, replacement) -> (existing + replacement) / 2  // 当有重复评分时取平均值
            ));

        Set<Integer> commonDoctors = new HashSet<>(ratings1.keySet());
        commonDoctors.retainAll(ratings2.keySet());
        log.info("用户 {} 和用户 {} 共同评价过 {} 个医生", 
                userId1, userId2, commonDoctors.size());

        if (commonDoctors.isEmpty()) {
            log.info("用户 {} 和用户 {} 没有共同评价的医生，相似度为0", userId1, userId2);
            return 0f;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (Integer doctorId : commonDoctors) {
            float r1 = ratings1.get(doctorId);
            float r2 = ratings2.get(doctorId);
            dotProduct += r1 * r2;
            norm1 += r1 * r1;
            norm2 += r2 * r2;
            log.debug("医生 {}: 用户 {} 评分={}, 用户 {} 评分={}", 
                    doctorId, userId1, r1, userId2, r2);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            log.info("用户评分向量为零，相似度为0");
            return 0f;
        }

        float similarity = (float) (dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2)));
        log.info("用户 {} 和用户 {} 的最终相似度: {}", userId1, userId2, similarity);
        return similarity;
    }

    @Override
    public String recommendDoctors(String symptoms, String departmentName, List<Map<String, Object>> doctors) {
        try {
            // 1. 提取症状关键词
            List<String> keySymptoms = extractKeywords(symptoms);
            log.info("从症状描述中提取的关键词: {}", keySymptoms);
            
            // 2. 计算每位医生与症状的匹配度
            List<Map<String, Object>> rankedDoctors = new ArrayList<>();
            StringBuilder reasoningProcess = new StringBuilder();
            
            reasoningProcess.append("## 症状分析\n");
            reasoningProcess.append("患者症状: ").append(symptoms).append("\n");
            reasoningProcess.append("症状关键词: ").append(String.join(", ", keySymptoms)).append("\n");
            reasoningProcess.append("推荐科室: ").append(departmentName).append("\n\n");
            
            reasoningProcess.append("## 医生匹配度分析\n");
            
            // 遍历所有医生，计算匹配分数
            for (Map<String, Object> doctor : doctors) {
                Map<String, Object> scoredDoctor = new HashMap<>(doctor);
                String doctorName = doctor.get("name").toString();
                String expertise = doctor.get("expertiseList") != null ? 
                                  doctor.get("expertiseList").toString() : "";
                String doctorDept = doctor.get("departmentName") != null ?
                                   doctor.get("departmentName").toString() : "";
                
                reasoningProcess.append("### 医生: ").append(doctorName).append("\n");
                reasoningProcess.append("- 科室: ").append(doctorDept).append("\n");
                reasoningProcess.append("- 专长: ").append(expertise).append("\n");
                
                // 计算专长匹配分数
                double expertiseScore = 0.0;
                List<String> matchedKeywords = new ArrayList<>();
                
                for (String symptom : keySymptoms) {
                    if (expertise.contains(symptom)) {
                        expertiseScore += 1.0;
                        matchedKeywords.add(symptom);
                    }
                }
                
                // 归一化专长匹配分数
                if (!keySymptoms.isEmpty()) {
                    expertiseScore = expertiseScore / keySymptoms.size();
                }
                
                // 科室匹配加分
                double departmentScore = 0.0;
                if (doctorDept.contains(departmentName)) {
                    departmentScore = 0.5;
                }
                
                // 医生评分
                Object ratingObj = doctor.get("average_rating");
                double ratingScore = 0.0;
                if (ratingObj != null) {
                    try {
                        ratingScore = Double.parseDouble(ratingObj.toString()) / 5.0; // 归一化到0-1
                    } catch (NumberFormatException e) {
                        ratingScore = 0.0;
                    }
                }
                
                // 计算综合分数: 50%专长匹配 + 30%科室匹配 + 20%评分
                double finalScore = expertiseScore * 0.5 + departmentScore * 0.3 + ratingScore * 0.2;
                
                // 记录每项分数
                reasoningProcess.append("- 专长匹配分数: ").append(String.format("%.2f", expertiseScore)).append("\n");
                reasoningProcess.append("  - 匹配关键词: ").append(
                    matchedKeywords.isEmpty() ? "无直接匹配" : String.join(", ", matchedKeywords)).append("\n");
                reasoningProcess.append("- 科室匹配分数: ").append(String.format("%.2f", departmentScore)).append("\n");
                reasoningProcess.append("- 医生评分分数: ").append(String.format("%.2f", ratingScore)).append("\n");
                reasoningProcess.append("- 综合评分: ").append(String.format("%.2f", finalScore)).append("\n\n");
                
                // 保存分数
                scoredDoctor.put("matchScore", finalScore);
                scoredDoctor.put("expertiseScore", expertiseScore);
                scoredDoctor.put("departmentScore", departmentScore);
                scoredDoctor.put("ratingScore", ratingScore);
                scoredDoctor.put("matchedKeywords", matchedKeywords);
                
                rankedDoctors.add(scoredDoctor);
            }
            
            // 3. 按综合分数排序
            rankedDoctors.sort((d1, d2) -> {
                Double score1 = (Double) d1.get("matchScore");
                Double score2 = (Double) d2.get("matchScore");
                return score2.compareTo(score1); // 降序排列
            });
            
            // 4. 生成推荐结果
            reasoningProcess.append("## 最终推荐排名\n");
            for (int i = 0; i < rankedDoctors.size(); i++) {
                Map<String, Object> doctor = rankedDoctors.get(i);
                reasoningProcess.append(i+1).append(". ")
                              .append(doctor.get("name"))
                              .append(" (综合评分: ")
                              .append(String.format("%.2f", doctor.get("matchScore")))
                              .append(")\n");
            }
            
            // 5. 使用AI生成最终推荐语
            StringBuilder prompt = new StringBuilder();
            prompt.append("作为医疗AI助手，请根据以下匹配分析，生成一段简洁的医生推荐：\n\n");
            prompt.append(reasoningProcess);
            prompt.append("\n请用通俗易懂的语言解释为什么这些医生适合患者的症状，特别是前三名医生的优势。");
            
            // 使用客服专用API
            String aiResponse = callZhipuApi(prompt.toString());
            JsonNode responseNode = objectMapper.readTree(aiResponse);
            String recommendation = "";
            
            if (responseNode.has("choices") && responseNode.get("choices").isArray() 
                    && responseNode.get("choices").size() > 0) {
                recommendation = responseNode.get("choices").get(0)
                              .get("message").get("content").asText();
            } else {
                // 如果AI生成失败，生成简单推荐
                recommendation = "根据您的症状分析，为您推荐以下医生：\n";
                for (int i = 0; i < Math.min(3, rankedDoctors.size()); i++) {
                    Map<String, Object> doctor = rankedDoctors.get(i);
                    recommendation += (i+1) + ". " + doctor.get("name") + "\n";
                }
            }
            
            // 6. 返回最终结果：AI推荐 + 推理过程
            return recommendation + "\n\n【推理详细过程】\n" + reasoningProcess.toString();
            
        } catch (Exception e) {
            log.error("混合推荐算法执行失败", e);
            return "抱歉，医生推荐过程中出现错误，请稍后再试。";
        }
    }
    
    /**
     * 提取症状关键词
     */
    private List<String> extractKeywords(String symptoms) {
        try {
            // 构建提示词
            String prompt = "请从以下症状描述中提取关键词，每行一个关键词：\n" + symptoms;
            
            String response = callZhipuApi(prompt);
            JsonNode responseNode = objectMapper.readTree(response);
            
            if (responseNode.has("choices") && responseNode.get("choices").isArray() 
                    && responseNode.get("choices").size() > 0) {
                String content = responseNode.get("choices").get(0)
                              .get("message").get("content").asText();
                
                List<String> keywords = Arrays.stream(content.split("\\n"))
                                .map(String::trim)
                                .filter(line -> !line.isEmpty())
                                .collect(Collectors.toList());
                
                return keywords;
            }
            
            // 如果AI提取失败，简单分词
            return Arrays.asList(symptoms.split("[,.，。、 ]"));
            
        } catch (Exception e) {
            log.error("提取关键词失败", e);
            // 出错时返回简单分词结果
            return Arrays.asList(symptoms.split("[,.，。、 ]"));
        }
    }
    
    /**
     * 调用智谱AI
     */
    private String callZhipuApi(String prompt) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_URL);
            
            // 使用统一的API Key
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + zhipuAiConfig.getApiKey());
            
            // 构建请求体
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", zhipuAiConfig.getModel()); // 使用统一的glm-4模型
            payload.put("temperature", 0.7);
            payload.put("max_tokens", 2048);
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", "你是一位专业的医疗AI助手，请提供详细的分析和推荐理由。");
            messages.add(systemMessage);
            
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);
            
            payload.put("messages", messages);
            
            // 转换为JSON
            String requestBody = objectMapper.writeValueAsString(payload);
            StringEntity entity = new StringEntity(requestBody, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            
            log.info("发送请求到智谱AI: {}", zhipuAiConfig.getApiKey());
            
            // 发送请求
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity responseEntity = response.getEntity();
                String responseBody = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                int statusCode = response.getStatusLine().getStatusCode();
                
                if (statusCode != 200) {
                    log.error("API调用失败，状态码: {}, 响应: {}", statusCode, responseBody);
                    throw new RuntimeException("API调用失败");
                }
                
                return responseBody;
            }
        }
    }
}