package com.sxt.controller;

import com.sxt.pojo.Result;
import com.sxt.service.SatisfactionAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/satisfaction")
public class SatisfactionAnalysisController {

    @Autowired
    private SatisfactionAnalysisService satisfactionAnalysisService;

    /**
     * 获取医生评价统计
     */
    @GetMapping("/doctor/{doctorId}")
    public Result<Map<String, Object>> getDoctorSatisfactionStats(@PathVariable Integer doctorId) {
        try {
            Map<String, Object> stats = satisfactionAnalysisService.getDoctorSatisfactionStats(doctorId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取医生评价统计失败, doctorId: {}", doctorId, e);
            return Result.error("获取医生评价统计失败");
        }
    }

    /**
     * 获取医生评价趋势
     */
    @GetMapping("/doctor/{doctorId}/trend")
    public Result<List<Map<String, Object>>> getDoctorSatisfactionTrend(
            @PathVariable Integer doctorId,
            @RequestParam String periodType,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            List<Map<String, Object>> trend = satisfactionAnalysisService.getDoctorSatisfactionTrend(
                    doctorId, periodType, startDate, endDate);
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取医生评价趋势失败, doctorId: {}", doctorId, e);
            return Result.error("获取医生评价趋势失败");
        }
    }

    /**
     * 获取科室评价统计
     */
    @GetMapping("/department/{departmentId}")
    public Result<Map<String, Object>> getDepartmentStats(@PathVariable Integer departmentId) {
        try {
            Map<String, Object> stats = satisfactionAnalysisService.getDepartmentStats(departmentId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取科室评价统计失败, departmentId: {}", departmentId, e);
            return Result.error("获取科室评价统计失败");
        }
    }

    /**
     * 获取科室评分分布
     */
    @GetMapping("/department/{departmentId}/distribution")
    public Result<List<Map<String, Object>>> getDepartmentRatingDistribution(@PathVariable Integer departmentId) {
        try {
            List<Map<String, Object>> distribution = satisfactionAnalysisService.getDepartmentRatingDistribution(departmentId);
            return Result.success(distribution);
        } catch (Exception e) {
            log.error("获取科室评分分布失败, departmentId: {}", departmentId, e);
            return Result.error("获取科室评分分布失败");
        }
    }

    /**
     * 获取科室评价趋势
     */
    @GetMapping("/department/{departmentId}/trend")
    public Result<List<Map<String, Object>>> getDepartmentRatingTrend(
            @PathVariable Integer departmentId,
            @RequestParam String periodType,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            List<Map<String, Object>> trend = satisfactionAnalysisService.getDepartmentRatingTrend(
                    departmentId, periodType, startDate, endDate);
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取科室评价趋势失败, departmentId: {}", departmentId, e);
            return Result.error("获取科室评价趋势失败");
        }
    }

    /**
     * 获取科室医生评分排名
     */
    @GetMapping("/department/{departmentId}/doctors")
    public Result<List<Map<String, Object>>> getDepartmentDoctorsRating(@PathVariable Integer departmentId) {
        try {
            List<Map<String, Object>> ratings = satisfactionAnalysisService.getDepartmentDoctorsRating(departmentId);
            return Result.success(ratings);
        } catch (Exception e) {
            log.error("获取科室医生评分排名失败, departmentId: {}", departmentId, e);
            return Result.error("获取科室医生评分排名失败");
        }
    }

    /**
     * 获取评分最高的医生
     */
    @GetMapping("/top-doctors")
    public Result<List<Map<String, Object>>> getTopRatedDoctors(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> doctors = satisfactionAnalysisService.getTopRatedDoctors(limit);
            return Result.success(doctors);
        } catch (Exception e) {
            log.error("获取评分最高的医生失败", e);
            return Result.error("获取评分最高的医生失败");
        }
    }

    /**
     * 获取评分分布
     */
    @GetMapping("/rating/distribution")
    public Result<List<Map<String, Object>>> getRatingDistribution() {
        try {
            List<Map<String, Object>> distribution = satisfactionAnalysisService.getRatingDistribution();
            return Result.success(distribution);
        } catch (Exception e) {
            log.error("获取评分分布失败", e);
            return Result.error("获取评分分布失败");
        }
    }

    /**
     * 获取评分趋势
     */
    @GetMapping("/rating/trend")
    public Result<List<Map<String, Object>>> getRatingTrend(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String periodType) {
        try {
            List<Map<String, Object>> trend = satisfactionAnalysisService.getRatingTrend(startDate, endDate, periodType);
            return Result.success(trend);
        } catch (Exception e) {
            log.error("获取评分趋势失败", e);
            return Result.error("获取评分趋势失败");
        }
    }

    /**
     * 获取时间段评价统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getReviewStatsByTimeRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            Map<String, Object> stats = satisfactionAnalysisService.getReviewStatsByTimeRange(startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取时间段评价统计失败", e);
            return Result.error("获取时间段评价统计失败");
        }
    }
} 