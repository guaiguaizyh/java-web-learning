package com.sxt.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SatisfactionAnalysisMapper {
    
    /**
     * 获取医生满意度统计
     */
    Map<String, Object> getDoctorSatisfactionStats(@Param("doctorId") Integer doctorId);
    
    /**
     * 获取医生满意度趋势
     */
    List<Map<String, Object>> getDoctorSatisfactionTrend(
            @Param("doctorId") Integer doctorId,
            @Param("dateFormat") String dateFormat,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);
    
    /**
     * 获取科室满意度统计
     */
    Map<String, Object> getDepartmentStats(@Param("departmentId") Integer departmentId);
    
    /**
     * 获取科室评分分布
     */
    List<Map<String, Object>> getDepartmentRatingDistribution(@Param("departmentId") Integer departmentId);
    
    /**
     * 获取科室评分趋势
     */
    List<Map<String, Object>> getDepartmentRatingTrend(
            @Param("departmentId") Integer departmentId,
            @Param("dateFormat") String dateFormat,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);
    
    /**
     * 获取科室医生评分排名
     */
    List<Map<String, Object>> getDepartmentDoctorsRating(@Param("departmentId") Integer departmentId);
    
    /**
     * 获取评分最高的医生
     */
    List<Map<String, Object>> getTopRatedDoctors(@Param("limit") int limit);
    
    /**
     * 获取评分分布
     */
    List<Map<String, Object>> getRatingDistribution();
    
    /**
     * 获取评分趋势
     */
    List<Map<String, Object>> getRatingTrend(
            @Param("dateFormat") String dateFormat,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);
    
    /**
     * 获取时间段评价统计
     */
    Map<String, Object> getReviewStatsByTimeRange(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);
} 