package com.sxt.service.impl;

import com.sxt.mapper.SatisfactionAnalysisMapper;
import com.sxt.service.SatisfactionAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SatisfactionAnalysisServiceImpl implements SatisfactionAnalysisService {

    @Autowired
    private SatisfactionAnalysisMapper satisfactionAnalysisMapper;

    @Override
    public Map<String, Object> getDoctorSatisfactionStats(Integer doctorId) {
        if (doctorId == null) {
            throw new IllegalArgumentException("医生ID不能为空");
        }
        return satisfactionAnalysisMapper.getDoctorSatisfactionStats(doctorId);
    }

    @Override
    public List<Map<String, Object>> getDoctorSatisfactionTrend(
            Integer doctorId, String periodType, String startDate, String endDate) {
        if (doctorId == null) {
            throw new IllegalArgumentException("医生ID不能为空");
        }
        validateDateParams(startDate, endDate, periodType);
        String dateFormat = getDateFormat(periodType);
        return satisfactionAnalysisMapper.getDoctorSatisfactionTrend(doctorId, dateFormat, startDate, endDate);
    }

    @Override
    public Map<String, Object> getDepartmentStats(Integer departmentId) {
        if (departmentId == null) {
            throw new IllegalArgumentException("科室ID不能为空");
        }
        return satisfactionAnalysisMapper.getDepartmentStats(departmentId);
    }

    @Override
    public List<Map<String, Object>> getDepartmentRatingDistribution(Integer departmentId) {
        if (departmentId == null) {
            throw new IllegalArgumentException("科室ID不能为空");
        }
        return satisfactionAnalysisMapper.getDepartmentRatingDistribution(departmentId);
    }

    @Override
    public List<Map<String, Object>> getDepartmentRatingTrend(
            Integer departmentId, String periodType, String startDate, String endDate) {
        if (departmentId == null) {
            throw new IllegalArgumentException("科室ID不能为空");
        }
        validateDateParams(startDate, endDate, periodType);
        String dateFormat = getDateFormat(periodType);
        return satisfactionAnalysisMapper.getDepartmentRatingTrend(departmentId, dateFormat, startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getDepartmentDoctorsRating(Integer departmentId) {
        if (departmentId == null) {
            throw new IllegalArgumentException("科室ID不能为空");
        }
        return satisfactionAnalysisMapper.getDepartmentDoctorsRating(departmentId);
    }

    @Override
    public List<Map<String, Object>> getTopRatedDoctors(int limit) {
        if (limit <= 0) {
            limit = 10; // 使用默认值
        }
        return satisfactionAnalysisMapper.getTopRatedDoctors(limit);
    }

    @Override
    public List<Map<String, Object>> getRatingDistribution() {
        return satisfactionAnalysisMapper.getRatingDistribution();
    }

    @Override
    public List<Map<String, Object>> getRatingTrend(String startDate, String endDate, String periodType) {
        validateDateParams(startDate, endDate, periodType);
        String dateFormat = getDateFormat(periodType);
        return satisfactionAnalysisMapper.getRatingTrend(dateFormat, startDate, endDate);
    }

    @Override
    public Map<String, Object> getReviewStatsByTimeRange(String startDate, String endDate) {
        validateDateParams(startDate, endDate, null);
        return satisfactionAnalysisMapper.getReviewStatsByTimeRange(startDate, endDate);
    }

    /**
     * 验证日期参数
     */
    private void validateDateParams(String startDate, String endDate, String periodType) {
        if (startDate == null || startDate.trim().isEmpty()) {
            throw new IllegalArgumentException("开始日期不能为空");
        }
        if (endDate == null || endDate.trim().isEmpty()) {
            throw new IllegalArgumentException("结束日期不能为空");
        }
        if (periodType != null && !periodType.trim().isEmpty()) {
            if (!periodType.matches("(?i)^(day|week|month)$")) {
                throw new IllegalArgumentException("统计周期类型必须是 day、week 或 month");
            }
        }
    }

    /**
     * 根据时间段类型获取日期格式
     */
    private String getDateFormat(String periodType) {
        if (periodType == null) {
            return "%Y-%m-%d";
        }
        switch (periodType.toLowerCase()) {
            case "day":
                return "%Y-%m-%d";
            case "week":
                return "%Y-%u";
            case "month":
                return "%Y-%m";
            default:
                return "%Y-%m-%d";
        }
    }
} 