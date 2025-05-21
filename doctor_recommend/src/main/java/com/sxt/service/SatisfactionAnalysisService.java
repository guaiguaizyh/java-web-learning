package com.sxt.service;

import java.util.List;
import java.util.Map;

public interface SatisfactionAnalysisService {
    
    /**
     * 获取医生满意度统计
     * @param doctorId 医生ID
     * @return 统计结果
     */
    Map<String, Object> getDoctorSatisfactionStats(Integer doctorId);
    
    /**
     * 获取医生满意度趋势
     * @param doctorId 医生ID
     * @param periodType 统计周期类型（day/week/month）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 趋势数据
     */
    List<Map<String, Object>> getDoctorSatisfactionTrend(Integer doctorId, String periodType, String startDate, String endDate);
    
    /**
     * 获取科室满意度统计
     * @param departmentId 科室ID
     * @return 统计结果
     */
    Map<String, Object> getDepartmentStats(Integer departmentId);
    
    /**
     * 获取科室评分分布
     * @param departmentId 科室ID
     * @return 分布数据
     */
    List<Map<String, Object>> getDepartmentRatingDistribution(Integer departmentId);
    
    /**
     * 获取科室评分趋势
     * @param departmentId 科室ID
     * @param periodType 统计周期类型（day/week/month）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 趋势数据
     */
    List<Map<String, Object>> getDepartmentRatingTrend(Integer departmentId, String periodType, String startDate, String endDate);
    
    /**
     * 获取科室医生评分排名
     * @param departmentId 科室ID
     * @return 排名数据
     */
    List<Map<String, Object>> getDepartmentDoctorsRating(Integer departmentId);
    
    /**
     * 获取评分最高的医生
     * @param limit 返回数量
     * @return 医生列表
     */
    List<Map<String, Object>> getTopRatedDoctors(int limit);
    
    /**
     * 获取评分分布
     * @return 分布数据
     */
    List<Map<String, Object>> getRatingDistribution();
    
    /**
     * 获取评分趋势
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param periodType 统计周期类型（day/week/month）
     * @return 趋势数据
     */
    List<Map<String, Object>> getRatingTrend(String startDate, String endDate, String periodType);
    
    /**
     * 获取时间段评价统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计结果
     */
    Map<String, Object> getReviewStatsByTimeRange(String startDate, String endDate);
} 