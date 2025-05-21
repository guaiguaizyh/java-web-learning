package com.sxt.service;

import java.util.List;
import java.util.Map;

public interface CollaborativeFilteringService {
    /**
     * 基于用户的协同过滤推荐
     * @param userId 用户ID
     * @param limit 返回结果数量限制
     * @return 医生ID到推荐分数的映射
     */
    Map<Integer, Float> getUserBasedRecommendations(Integer userId, int limit);

    /**
     * 基于专长的协同过滤推荐
     * @param expertiseIds 专长ID列表
     * @param limit 返回结果数量限制
     * @return 医生ID到推荐分数的映射
     */
    Map<Integer, Float> getExpertiseBasedRecommendations(List<Integer> expertiseIds, int limit);

    /**
     * 计算用户相似度
     * @param userId1 用户1的ID
     * @param userId2 用户2的ID
     * @return 相似度分数
     */
    float calculateUserSimilarity(Integer userId1, Integer userId2);

    /**
     * 计算医生相似度
     * @param doctorId1 医生1的ID
     * @param doctorId2 医生2的ID
     * @return 相似度分数
     */
    float calculateDoctorSimilarity(Integer doctorId1, Integer doctorId2);

    /**
     * 更新用户对医生的评分
     * @param userId 用户ID
     * @param doctorId 医生ID
     * @param rating 评分
     */
    void updateUserRating(Integer userId, Integer doctorId, Float rating);
} 