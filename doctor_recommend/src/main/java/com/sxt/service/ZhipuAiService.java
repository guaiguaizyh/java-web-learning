package com.sxt.service;

import java.util.List;
import java.util.Map;

/**
 * 智谱AI服务接口
 */
public interface ZhipuAiService {
    
    /**
     * 聊天
     * @param prompt 用户输入
     * @return AI响应
     */
    String chat(String prompt);
    
    /**
     * 带上下文的聊天
     * @param prompt 用户输入
     * @param context 上下文信息
     * @return AI响应
     */
    String chat(String prompt, String context);
    
    /**
     * 从自然语言描述中提取关键词
     * @param description 自然语言描述
     * @return 提取的关键词列表
     */
    List<String> extractKeywords(String description);
    
    /**
     * 分析症状并推荐相关专长
     * @param symptomDescription 症状描述
     * @return 推荐的专长列表
     */
    List<String> analyzeSymptoms(String symptomDescription);
    
    /**
     * 推荐科室
     * @param symptomDescription 症状描述
     * @return 推荐的科室
     */
    String recommendDepartment(String symptomDescription);
    
    /**
     * 推荐医生并生成解释
     * @param symptoms 症状描述
     * @param departmentName 科室名称
     * @param doctors 医生列表
     * @return 带有解释的医生推荐
     */
    String recommendDoctorsWithExplanation(String symptoms, String departmentName, List<Map<String, Object>> doctors);

    /**
     * 评估回答的专业度分数
     * @param question 问题
     * @param answer 回答
     * @return 专业度分数
     */
    float evaluateProfessionalScore(String question, String answer);

    /**
     * 收集和分析用户症状信息
     * @param conversation 对话历史
     * @return 症状信息
     */
    Map<String, Object> collectSymptomInfo(List<String> conversation);

    /**
     * 动态调整医生专长权重
     * @param doctorId 医生ID
     * @param symptomDescription 症状描述
     * @return 权重分数
     */
    float calculateDynamicExpertiseWeight(Integer doctorId, String symptomDescription);
} 