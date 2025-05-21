package com.sxt.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sxt.config.ZhipuAiConfig;
import com.sxt.service.ZhipuAiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.sxt.pojo.Doctor;
import com.sxt.mapper.DoctorMapper;

/**
 * 智谱AI服务实现类 - A
 */
@Service
@Slf4j
public class ZhipuAiServiceImpl implements ZhipuAiService {

    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    
    @Autowired
    private ZhipuAiConfig zhipuAiConfig;
    
    @Autowired
    private DoctorMapper doctorMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String chat(String prompt) {
        return chat(prompt, "");
    }
    
    @Override
    public String chat(String prompt, String context) {
        try {
            log.info("发送聊天请求到智谱AI: {}, 上下文: {}", prompt, context);
            
            // 1. 检查是否包含症状相关内容
            if (containsSymptomKeywords(prompt)) {
                // 2. 提取症状关键词
                List<String> keywords = extractKeywords(prompt);
                
                // 3. 查询数据库中相关的专长和医生
                List<Map<String, Object>> relevantDoctors = doctorMapper.findDoctorsByKeywords(keywords);
                
                // 4. 构建包含医生信息的提示词
                String enhancedPrompt = buildPromptWithDoctorInfo(prompt, relevantDoctors);
                if (!context.isEmpty()) {
                    enhancedPrompt = "上下文信息：" + context + "\n\n" + enhancedPrompt;
                }
                
                // 5. 调用AI生成回复
                String response = callZhipuApi(createChatPayload(enhancedPrompt));
                JsonNode responseNode = objectMapper.readTree(response);
                
                if (responseNode.has("choices") && responseNode.get("choices").isArray() 
                        && responseNode.get("choices").size() > 0) {
                    String content = responseNode.get("choices").get(0)
                            .get("message").get("content").asText();
                    log.info("智谱AI响应: {}", content);
                    return content;
                }
            } else {
                // 普通对话模式
                String fullPrompt = prompt;
                if (!context.isEmpty()) {
                    fullPrompt = "上下文信息：" + context + "\n\n用户问题：" + prompt;
                }
                
                String response = callZhipuApi(createChatPayload(fullPrompt));
                JsonNode responseNode = objectMapper.readTree(response);
                
                if (responseNode.has("choices") && responseNode.get("choices").isArray() 
                        && responseNode.get("choices").size() > 0) {
                    String content = responseNode.get("choices").get(0)
                            .get("message").get("content").asText();
                    log.info("智谱AI响应: {}", content);
                    return content;
                }
            }
            
            return "抱歉，我现在无法正确理解您的问题。请您换个方式描述，或稍后再试。";
            
        } catch (Exception e) {
            log.error("AI聊天服务异常", e);
            return "抱歉，系统暂时遇到问题。如果您有紧急情况，请直接联系医院或拨打急诊电话。";
        }
    }
    
    @Override
    public String recommendDepartment(String symptomDescription) {
        try {
            log.info("发送科室推荐请求到智谱AI: {}", symptomDescription);
            
            // 1. 获取数据库中的所有科室
            List<String> availableDepartments = doctorMapper.getAllDepartmentNames();
            if (availableDepartments == null || availableDepartments.isEmpty()) {
                log.warn("数据库中没有科室信息");
                return "抱歉，系统暂时无法提供科室推荐。请直接联系医院进行咨询。";
            }
            
            // 2. 构建更精确的提示词，包含实际可用的科室列表
            String prompt = String.format(
                "作为一个专业的医疗顾问，请仔细分析以下患者描述的症状：\n\n%s\n\n" +
                "我们医院目前开设的科室有：\n%s\n\n" +
                "请根据患者症状，从以上科室列表中进行推荐。\n\n" +
                "要求：\n" +
                "1. 必须从提供的科室列表中选择，不要推荐列表外的科室\n" +
                "2. 可以推荐1-2个最相关的科室\n" +
                "3. 详细说明推荐这些科室的原因\n" +
                "4. 如果症状可能涉及多个科室，请说明主次关系\n\n" +
                "请按以下格式回复：\n" +
                "建议就诊科室：[科室名称]\n" +
                "理由：[详细解释]\n" +
                "请用简洁专业的语言回答。",
                symptomDescription,
                String.join("、", availableDepartments)
            );
            
            String response = callZhipuApi(createChatPayload(prompt));
            JsonNode responseNode = objectMapper.readTree(response);
            
            // 解析响应
            if (responseNode.has("choices") && responseNode.get("choices").isArray() 
                    && responseNode.get("choices").size() > 0) {
                String content = responseNode.get("choices").get(0)
                        .get("message").get("content").asText();
                log.info("智谱AI科室推荐响应: {}", content);
                
                // 验证AI推荐的科室是否在可用科室列表中
                List<String> recommendedDepts = extractDepartments(content);
                recommendedDepts.removeIf(dept -> !availableDepartments.contains(dept));
                
                if (recommendedDepts.isEmpty()) {
                    log.warn("AI推荐的科室都不在可用科室列表中");
                    return "抱歉，系统无法根据您的症状找到合适的科室。建议您：\n" +
                           "1. 可以尝试更详细地描述您的症状\n" +
                           "2. 建议您直接前往医院就医，由医生进行诊断\n" +
                           "3. 如果是急症，请立即前往急诊科就医";
                }
                
                return content;
            } else {
                log.error("无法从响应中解析出内容: {}", response);
                return "抱歉，无法获取科室推荐。";
            }
        } catch (Exception e) {
            log.error("调用智谱AI科室推荐服务失败", e);
            return "抱歉，AI推荐服务暂时无法使用，请稍后再试。";
        }
    }
    
    @Override
    public String recommendDoctorsWithExplanation(String symptomDescription, String departmentName, List<Map<String, Object>> recommendedDoctors) {
        try {
            log.info("发送医生推荐请求到智谱AI，症状: {}, 科室: {}, 医生数量: {}", 
                    symptomDescription, departmentName, recommendedDoctors.size());
            
            // 构建包含医生信息的字符串
            StringBuilder doctorsInfo = new StringBuilder();
            for (int i = 0; i < recommendedDoctors.size(); i++) {
                Map<String, Object> doctor = recommendedDoctors.get(i);
                doctorsInfo.append(i + 1).append(". ")
                          .append("医生姓名: ").append(doctor.get("name")).append("\n")
                          .append("   ⭐ 职称: ").append(doctor.get("positionsName")).append("\n")
                          .append("   📍 所在科室: ").append(doctor.get("departmentName")).append("\n")
                          .append("   💫 专长: ").append(doctor.get("expertiseList")).append("\n");
                
                // 添加评分信息
                Object rating = doctor.get("averageRating");
                Object ratingCount = doctor.get("ratingCount");
                if (rating != null) {
                    doctorsInfo.append("   ⭐ 评分: ").append(rating);
                    if (ratingCount != null) {
                        doctorsInfo.append(" (").append(ratingCount).append("条评价)");
                    }
                    doctorsInfo.append("\n");
                }
                
                // 添加工作年限
                Object workYears = doctor.get("workYears");
                if (workYears != null) {
                    doctorsInfo.append("   🎯 从业年限: ").append(workYears).append("年\n");
                }
                
                // 添加出诊时间（如果有）
                Object schedule = doctor.get("schedule");
                if (schedule != null && !schedule.toString().isEmpty()) {
                    doctorsInfo.append("   🕒 出诊时间: ").append(schedule).append("\n");
                }
                
                // 添加擅长疾病（如果有）
                Object goodAtDiseases = doctor.get("goodAtDiseases");
                if (goodAtDiseases != null && !goodAtDiseases.toString().isEmpty()) {
                    doctorsInfo.append("   🏥 擅长疾病: ").append(goodAtDiseases).append("\n");
                }
                
                doctorsInfo.append("\n");
            }
            
            // 构建提示词，引导AI给出更精确的医生推荐和解释
            String prompt = String.format(
                "患者症状：%s\n\n" +
                "推荐科室：%s\n\n" +
                "可选医生信息：\n%s\n" +
                "请根据患者症状和医生的专业特长、评分、从业经验等信息，推荐最适合的1-2名医生。\n" +
                "回答要点：\n" +
                "1. 直接说明推荐哪位医生\n" +
                "2. 解释为什么推荐这位医生（结合医生专长、经验和患者症状）\n" +
                "3. 如果推荐多位医生，说明各自的优势\n" +
                "请用100-150字简洁专业地回答。",
                symptomDescription, departmentName, doctorsInfo.toString());
            
            String response = callZhipuApi(createChatPayloadWithMedicalPrompt(prompt));
            JsonNode responseNode = objectMapper.readTree(response);
            
            // 解析响应
            if (responseNode.has("choices") && responseNode.get("choices").isArray() 
                    && responseNode.get("choices").size() > 0) {
                String content = responseNode.get("choices").get(0)
                        .get("message").get("content").asText();
                log.info("智谱AI医生推荐响应: {}", content);
                return content;
            } else {
                log.error("无法从响应中解析出内容: {}", response);
                return "抱歉，无法获取医生推荐。";
            }
        } catch (Exception e) {
            log.error("调用智谱AI医生推荐服务失败", e);
            return "抱歉，AI推荐服务暂时无法使用，请稍后再试。";
        }
    }
    
    /**
     * 创建聊天请求的JSON负载
     */
    private String createChatPayload(String prompt) throws Exception {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", zhipuAiConfig.getModel());
        payload.put("temperature", zhipuAiConfig.getTemperature());
        payload.put("max_tokens", zhipuAiConfig.getMaxTokens());
        
        ArrayNode messages = objectMapper.createArrayNode();
        
        // 系统消息
        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一位专业的医疗咨询助手。请提供简短、清晰的医疗建议，每次回复控制在50-200字以内。\n" +
                "回答要点：\n" +
                "1. 简明提供合适的就诊科室建议\n" +
                "2. 简要解释理由\n" +
                "3. 症状严重时，直接建议及时就医\n" +
                "请避免冗长的解释，保持回复简洁且专业");
        messages.add(systemMessage);
        
        // 用户消息
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);
        
        payload.set("messages", messages);
        
        return objectMapper.writeValueAsString(payload);
    }
    
    /**
     * 创建医生推荐专用的聊天请求JSON负载
     */
    private String createChatPayloadWithMedicalPrompt(String prompt) throws Exception {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", zhipuAiConfig.getModel());
        payload.put("temperature", zhipuAiConfig.getTemperature());
        payload.put("max_tokens", zhipuAiConfig.getMaxTokens());
        
        ArrayNode messages = objectMapper.createArrayNode();
        
        // 系统消息
        ObjectNode systemMessage = objectMapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一位简洁的医疗顾问。请基于患者症状和医生信息，提供简短的推荐（50-100字）。直接推荐最匹配的医生，避免冗长解释。");
        messages.add(systemMessage);
        
        // 用户消息
        ObjectNode userMessage = objectMapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);
        
        payload.set("messages", messages);
        
        return objectMapper.writeValueAsString(payload);
    }
    
    /**
     * 调用智谱AI API
     */
    private String callZhipuApi(String requestBody) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(API_URL);
            
            // 打印API密钥进行调试
            log.info("当前使用的API Key: {}", zhipuAiConfig.getApiKey());
            
            // 设置请求头
            httpPost.setHeader("Content-Type", "application/json");
            // 使用配置的API Key
            httpPost.setHeader("Authorization", "Bearer " + zhipuAiConfig.getApiKey());
            
            // 打印请求体进行调试
            log.debug("发送请求体: {}", requestBody);
            
            // 设置请求体
            StringEntity entity = new StringEntity(requestBody, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            
            // 发送请求并获取响应
            log.info("开始发送请求到智谱AI: {}", API_URL);
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                HttpEntity responseEntity = response.getEntity();
                String responseBody = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
                int statusCode = response.getStatusLine().getStatusCode();
                
                log.info("收到智谱AI响应，状态码: {}", statusCode);
                
                if (statusCode != 200) {
                    log.error("调用智谱AI API失败，状态码: {}, 响应: {}", 
                             statusCode, responseBody);
                    throw new RuntimeException("API调用失败，状态码: " + statusCode);
                }
                
                log.debug("成功获取响应: {}", responseBody);
                return responseBody;
            }
        } catch (Exception e) {
            log.error("调用智谱AI API过程中发生异常", e);
            throw e;
        }
    }
    
    /**
     * 生成签名 (如果智谱API需要)
     */
    private String generateSignature(long timestamp) throws Exception {
        String apiKey = zhipuAiConfig.getApiKey();
        // 解析apiKey，通常格式为id.secret
        String[] parts = apiKey.split("\\.");
        
        if (parts.length != 2) {
            log.warn("API Key格式不符合预期，无法生成签名");
            return "";
        }
        
        String id = parts[0];
        String secret = parts[1];
        
        String payload = timestamp + "\n" + id;
        
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSha256.init(secretKey);
        byte[] hash = hmacSha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        
        return Base64.getEncoder().encodeToString(hash);
    }

    @Override
    public List<String> extractKeywords(String description) {
        try {
            log.info("开始提取关键词: {}", description);
            
            // 构建更精确的提示词，明确要求只返回关键词
            String prompt = String.format(
                "请从以下症状描述中提取关键症状词，要求：\n" +
                "1. 每行只返回一个症状关键词\n" +
                "2. 不要包含箭头、逗号等标点符号\n" +
                "3. 不要包含就诊建议和科室信息\n" +
                "4. 只返回症状本身\n\n" +
                "症状描述：%s\n\n" +
                "示例格式：\n" +
                "头痛\n" +
                "头晕\n" +
                "恶心", 
                description
            );
            
            String response = callZhipuApi(createChatPayload(prompt));
            JsonNode responseNode = objectMapper.readTree(response);
            
            if (responseNode.has("choices") && responseNode.get("choices").isArray() 
                    && responseNode.get("choices").size() > 0) {
                String content = responseNode.get("choices").get(0)
                        .get("message").get("content").asText();
                
                // 将AI响应按行分割，过滤空行和无效内容
                List<String> keywords = Arrays.stream(content.split("\\n"))
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .filter(line -> !line.contains("->"))  // 过滤包含箭头的行
                        .filter(line -> !line.contains("："))  // 过滤包含冒号的行
                        .filter(line -> line.length() >= 2)    // 过滤过短的词
                        .filter(line -> !line.matches(".*[,.，。、；;].*"))  // 过滤包含标点的行
                        .collect(Collectors.toList());
                
                log.info("提取的关键词: {}", keywords);
                return keywords;
            } else {
                log.error("无法从响应中解析出关键词: {}", response);
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("调用智谱AI提取关键词失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> analyzeSymptoms(String symptomDescription) {
        try {
            log.info("开始分析症状: {}", symptomDescription);
            
            // 构建提示词，引导AI分析症状并推荐相关专长
            String prompt = String.format(
                "请分析以下症状描述，列出最相关的医生专长（专业领域）：\n" +
                "症状描述：%s\n" +
                "请直接列出专长名称，每行一个，不要添加任何其他解释。例如：\n" +
                "心血管内科\n" +
                "高血压\n" +
                "冠心病", 
                symptomDescription
            );
            
            String response = callZhipuApi(createChatPayload(prompt));
            JsonNode responseNode = objectMapper.readTree(response);
            
            if (responseNode.has("choices") && responseNode.get("choices").isArray() 
                    && responseNode.get("choices").size() > 0) {
                String content = responseNode.get("choices").get(0)
                        .get("message").get("content").asText();
                
                // 将AI响应按行分割，过滤空行
                List<String> expertises = Arrays.stream(content.split("\\n"))
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .collect(Collectors.toList());
                
                log.info("AI分析出的专长: {}", expertises);
                return expertises;
            } else {
                log.error("无法从响应中解析出专长列表: {}", response);
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("调用智谱AI分析症状失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public float evaluateProfessionalScore(String question, String answer) {
        float score = 0.0f;
        
        // 1. 检查回答中是否包含专业医学术语
        List<String> medicalTerms = extractMedicalTerms(answer);
        if (!medicalTerms.isEmpty()) {
            score += 0.3f;
        }
        
        // 2. 检查回答的结构完整性
        if (answer.contains("建议") || answer.contains("诊断") || answer.contains("治疗")) {
            score += 0.2f;
        }
        
        // 3. 检查是否包含解释说明
        if (answer.contains("因为") || answer.contains("原因") || answer.contains("所以")) {
            score += 0.2f;
        }
        
        // 4. 检查是否有预防建议
        if (answer.contains("预防") || answer.contains("注意事项") || answer.contains("避免")) {
            score += 0.2f;
        }
        
        // 5. 检查是否有进一步就医建议
        if (answer.contains("建议就医") || answer.contains("及时就医") || answer.contains("复诊")) {
            score += 0.1f;
        }
        
        return Math.min(1.0f, score);
    }

    @Override
    public Map<String, Object> collectSymptomInfo(List<String> conversation) {
        Map<String, Object> symptomInfo = new HashMap<>();
        List<String> symptoms = new ArrayList<>();
        List<String> duration = new ArrayList<>();
        List<String> severity = new ArrayList<>();
        
        for (String message : conversation) {
            // 提取症状
            List<String> extractedSymptoms = extractSymptomKeywords(message);
            symptoms.addAll(extractedSymptoms);
            
            // 提取持续时间
            if (message.contains("天") || message.contains("周") || message.contains("月")) {
                duration.add(extractDuration(message));
            }
            
            // 提取严重程度
            if (message.contains("疼") || message.contains("痛") || message.contains("不适")) {
                severity.add(extractSeverity(message));
            }
        }
        
        symptomInfo.put("symptoms", symptoms);
        symptomInfo.put("duration", duration);
        symptomInfo.put("severity", severity);
        
        return symptomInfo;
    }

    @Override
    public float calculateDynamicExpertiseWeight(Integer doctorId, String symptomDescription) {
        float baseWeight = 0.7f; // 提高基础权重
        
        try {
            // 1. 根据症状关键词匹配度调整权重 (占比提升到30%)
            List<String> doctorExpertise = Arrays.asList(doctorMapper.selectById(doctorId).getExpertiseList().split(","));
            List<String> symptomKeywords = extractKeywords(symptomDescription);
            float matchRate = calculateMatchRate(doctorExpertise, symptomKeywords);
            baseWeight += matchRate * 0.3f;
            
            log.info("医生ID: {}, 专长匹配度: {}", doctorId, matchRate);
            
            return Math.min(1.0f, baseWeight);
        } catch (Exception e) {
            log.error("计算医生专长权重时发生错误, doctorId: {}", doctorId, e);
            return baseWeight; // 发生错误时返回基础权重
        }
    }

    // 辅助方法

    private List<String> extractMedicalTerms(String text) {
        List<String> terms = new ArrayList<>();
        // 使用医学术语词典进行匹配
        String[] medicalDictionary = {"症状", "诊断", "治疗", "病因", "并发症", "预后"};
        for (String term : medicalDictionary) {
            if (text.contains(term)) {
                terms.add(term);
            }
        }
        return terms;
    }

    private String extractDuration(String message) {
        // 使用正则表达式提取持续时间
        Pattern pattern = Pattern.compile("(\\d+)([天周月年])");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    private String extractSeverity(String message) {
        // 根据关键词判断严重程度
        if (message.contains("剧烈") || message.contains("严重")) {
            return "严重";
        } else if (message.contains("中度") || message.contains("一般")) {
            return "中度";
        } else {
            return "轻度";
        }
    }

    private float calculateMatchRate(List<String> expertise, List<String> symptoms) {
        if (expertise.isEmpty() || symptoms.isEmpty()) {
            return 0.0f;
        }
        
        int matches = 0;
        for (String symptom : symptoms) {
            for (String exp : expertise) {
                if (exp.contains(symptom) || symptom.contains(exp)) {
                    matches++;
                    break;
                }
            }
        }
        
        return (float) matches / symptoms.size();
    }

    private List<String> extractSymptomKeywords(String message) {
        // 使用已有的extractKeywords方法
        return extractKeywords(message);
    }

    /**
     * 构建包含医生信息的提示词
     */
    private String buildPromptWithDoctorInfo(String userPrompt, List<Map<String, Object>> doctors) {
        StringBuilder enhancedPrompt = new StringBuilder();
        enhancedPrompt.append("用户描述：").append(userPrompt).append("\n\n");
        
        if (!doctors.isEmpty()) {
            enhancedPrompt.append("根据数据库查询，以下是可能相关的医生信息：\n");
            for (Map<String, Object> doctor : doctors) {
                enhancedPrompt.append("- ")
                    .append(doctor.get("name"))
                    .append("，")
                    .append(doctor.get("departmentName"))
                    .append("，专长：")
                    .append(doctor.get("expertiseList"))
                    .append("\n");
            }
            enhancedPrompt.append("\n请结合以上医生信息，给出专业的建议和解释。");
        }
        
        return enhancedPrompt.toString();
    }

    /**
     * 检查是否包含症状关键词
     */
    private boolean containsSymptomKeywords(String message) {
        String[] keywords = {
            "疼", "痛", "不舒服", "症状", "难受", "头晕", "发热", "感冒", "发烧", 
            "咳嗽", "喘", "腹泻", "恶心", "呕吐", "过敏", "出疹", "失眠",
            "头", "胸", "腹", "背", "腰", "脖子", "手", "脚", "关节", "肚子",
            "肿", "痒", "麻", "胀", "酸", "晕", "困", "累", "虚弱", "乏力"
        };
        
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从AI响应中提取科室名称列表
     */
    private List<String> extractDepartments(String aiResponse) {
        List<String> departments = new ArrayList<>();
        Pattern pattern = Pattern.compile("建议(?:就诊|到|看|选择)?(?:的)?科室[：:](.*?)(?=\\n|$)");
        Matcher matcher = pattern.matcher(aiResponse);
        
        if (matcher.find()) {
            String departmentStr = matcher.group(1).trim();
            String[] depts = departmentStr.split("[、，,。]");
            for (String dept : depts) {
                String cleanDept = dept.trim()
                    .replaceAll("[（(][^）)]*[）)]", "") // 移除括号内容
                    .replaceAll("^(去|到|选择|建议|推荐)", "") // 移除前缀动词
                    .trim();
                if (!cleanDept.isEmpty()) {
                    departments.add(cleanDept);
                }
            }
        }
        
        return departments;
    }
} 