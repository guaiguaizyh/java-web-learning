// src/main/java/com/sxt/service/DoctorRecommendService.java
package com.sxt.service;

import com.sxt.pojo.RecommendedDoctorDTO;
import java.util.List;
import java.util.Map;

/**
 * 医生推荐服务接口
 */
public interface DoctorRecommendService {
    /**
     * 基于自然语言描述推荐医生
     * @param naturalLanguageDescription 用户输入的自然语言症状描述
     * @param userId 用户ID（可选，用于个性化推荐）
     * @param limit 返回的医生数量
     * @return 推荐的医生列表
     */
    List<RecommendedDoctorDTO> recommendDoctorsByNaturalLanguage(String naturalLanguageDescription, Integer userId, int limit);
    
    /**
     * 基于AI的直接推荐方法
     * 使用智谱AI分析症状并推荐专长
     * 
     * @param symptomDescription 用户输入的症状描述
     * @param userId 用户ID（可选，用于个性化推荐）
     * @param limit 返回的医生数量限制
     * @return 推荐的医生列表
     */
    List<RecommendedDoctorDTO> aiDirectRecommendation(String symptomDescription, Integer userId, int limit);
    
    /**
     * 根据症状分析并生成医生推荐文本
     * 
     * @param symptoms 症状描述
     * @param departmentName 科室名称
     * @param doctors 医生列表
     * @return 推荐结果文本
     */
    String recommendDoctors(String symptoms, String departmentName, List<Map<String, Object>> doctors);
    
    /**
     * 查找评分最高的 Top N 医生（冷启动推荐）
     * @param limit 返回的医生数量
     * @return 推荐的医生列表
     */
    List<RecommendedDoctorDTO> findTopRatedDoctors(int limit);
    
    /**
     * 基于用户病历记录推荐医生
     * @param userId 用户ID
     * @param limit 返回结果限制
     * @return 医生推荐列表
     */
    List<RecommendedDoctorDTO> recommendDoctorsByUserProfile(Integer userId, int limit);

    /**
     * 获取基于用户的协同过滤推荐分数
     * @param userId 用户ID
     * @param limit 返回结果数量限制
     * @return 医生ID到推荐分数的映射
     */
    Map<Integer, Float> getUserBasedRecommendations(Integer userId, int limit);

    /**
     * 计算医生的评分分数
     * @param doctor 医生DTO对象
     * @return 评分分数（0-1之间的浮点数）
     */
    float calculateRatingScore(RecommendedDoctorDTO doctor);
}
