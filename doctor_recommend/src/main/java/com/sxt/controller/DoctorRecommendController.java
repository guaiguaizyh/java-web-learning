// src/main/java/com/sxt/controller/DoctorRecommendController.java
package com.sxt.controller;

import com.sxt.pojo.RecommendedDoctorDTO;
import com.sxt.pojo.Result;
import com.sxt.service.DoctorRecommendService;
import com.sxt.service.UserService;
import com.sxt.mapper.UserMapper;
import com.sxt.pojo.User;
import com.sxt.service.ZhipuAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 医生推荐控制器
 * 提供统一的医生推荐接口，支持多种推荐策略
 */
@RestController
@RequestMapping("/recommend/doctors")
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8080"}, allowCredentials = "true")
public class DoctorRecommendController {
    
    @Autowired
    private DoctorRecommendService doctorRecommendService;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ZhipuAiService zhipuAiService;
    
    /**
     * 统一的医生推荐接口
     * 支持多种推荐方式：
     * - AI：使用智谱AI进行关键词提取，然后用自然语言处理推荐
     * - NATURAL：使用本地分词进行自然语言处理推荐
     * - PROFILE：基于用户画像推荐
     * 
     * 请求示例：
     * {
     *     "description": "头痛发烧", // 症状描述（可选）
     *     "userId": 123,           // 用户ID（可选）
     *     "limit": 10,            // 返回数量（可选，默认10）
     *     "recommendType": "AI"    // 推荐类型（可选，默认NATURAL）
     * }
     * 
     * @param requestBody 包含推荐所需参数的请求体
     * @return 推荐医生列表
     */
    @PostMapping("")
    public Result<List<RecommendedDoctorDTO>> recommendDoctors(@RequestBody Map<String, Object> requestBody) {
        // 提取请求参数
        String description = (String) requestBody.getOrDefault("description", "");
        Integer userId = requestBody.containsKey("userId") ? 
                        Integer.valueOf(requestBody.get("userId").toString()) : null;
        Integer limit = requestBody.containsKey("limit") ? 
                       Integer.valueOf(requestBody.get("limit").toString()) : 10;
        String recommendType = ((String) requestBody.getOrDefault("recommendType", "NATURAL")).toUpperCase();
        
        log.info("接收到医生推荐请求 - 描述: {}, 用户ID: {}, 数量限制: {}, 推荐类型: {}", 
                description, userId, limit, recommendType);
        
        try {
            List<RecommendedDoctorDTO> recommendedDoctors;
            
            // 如果是基于用户画像推荐，必须提供用户ID
            if ("PROFILE".equals(recommendType) && userId == null) {
                return Result.error("基于用户画像推荐时，用户ID不能为空");
            }
            
            // 如果指定了用户ID但没有提供症状描述，尝试从用户的医疗记录中获取
            if ((description == null || description.trim().isEmpty() || "无".equals(description.trim())) 
                && userId != null) {
                User user = userMapper.selectById(userId.longValue());
                if (user != null && user.getMedicalRecord() != null && !user.getMedicalRecord().trim().isEmpty()) {
                    description = user.getMedicalRecord();
                    log.info("使用用户 {} 的医疗记录作为症状描述: {}", userId, description);
                }
            }
            
            // 处理空描述情况
            if (description == null || description.trim().isEmpty() || "无".equals(description.trim())) {
                log.info("症状描述为空，返回评分最高的医生");
                return Result.success(doctorRecommendService.findTopRatedDoctors(limit));
            }
            
            // AI模式的特殊处理：使用AI提取关键词，然后使用自然语言处理推荐
            if ("AI".equals(recommendType)) {
                log.info("使用AI模式提取关键词");
                try {
                    // 调用AI提取关键词
                    List<String> aiKeywords = zhipuAiService.extractKeywords(description);
                    
                    if (aiKeywords != null && !aiKeywords.isEmpty()) {
                        log.info("AI成功提取关键词: {}", aiKeywords);
                        
                        // 组合关键词为一个句子
                        String keywordDescription = String.join("，", aiKeywords);
                        log.info("组合后的关键词描述: {}", keywordDescription);
                        
                        // 使用关键词调用自然语言处理推荐
                        recommendedDoctors = doctorRecommendService.recommendDoctorsByNaturalLanguage(
                            keywordDescription, userId, limit);
                    } else {
                        log.warn("AI提取关键词失败，降级为自然语言处理");
                        recommendedDoctors = doctorRecommendService.recommendDoctorsByNaturalLanguage(
                            description, userId, limit);
                    }
                } catch (Exception e) {
                    log.error("AI提取关键词异常，降级为自然语言处理", e);
                    recommendedDoctors = doctorRecommendService.recommendDoctorsByNaturalLanguage(
                        description, userId, limit);
                }
            } else if ("PROFILE".equals(recommendType)) {
                log.info("使用自然语言处理推荐 (用户画像基于医疗记录)");
                recommendedDoctors = doctorRecommendService.recommendDoctorsByNaturalLanguage(
                    description, userId, limit);
            } else {
                // NATURAL或其他默认模式
                log.info("使用自然语言处理推荐");
                recommendedDoctors = doctorRecommendService.recommendDoctorsByNaturalLanguage(
                    description, userId, limit);
            }
            
            // 更新: 不再过滤匹配度低的结果，即使匹配度较低也返回
            // 如果完全没有结果，才返回评分最高的医生
            if (recommendedDoctors == null || recommendedDoctors.isEmpty()) {
                log.info("推荐服务没有返回任何结果，返回评分最高的医生");
                recommendedDoctors = doctorRecommendService.findTopRatedDoctors(limit);
            } else {
                // 这里不再过滤推荐结果，保留所有匹配医生，包括匹配度较低的
                log.info("成功找到 {} 个推荐医生，包含各种匹配度的结果", recommendedDoctors.size());
            }
            
            // 添加匹配度分组标记，前端可以据此分组显示
            for (RecommendedDoctorDTO doctor : recommendedDoctors) {
                if (doctor.getMatchScore() > 0.7f) {
                    doctor.setMatchLabel("最佳匹配");
                } else if (doctor.getMatchScore() > 0.4f) {
                    doctor.setMatchLabel("相关匹配");
                } else {
                    doctor.setMatchLabel("其他医生");
                }
            }
            
            return Result.success(recommendedDoctors);
            
        } catch (Exception e) {
            log.error("推荐医生失败", e);
            return Result.error("推荐失败：" + e.getMessage());
        }
    }
}