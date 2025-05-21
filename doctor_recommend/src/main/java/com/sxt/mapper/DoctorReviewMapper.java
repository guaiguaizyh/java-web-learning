package com.sxt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sxt.pojo.DoctorReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DoctorReviewMapper extends BaseMapper<DoctorReview> {

    /**
     * 分页查询指定医生的评论列表，并关联用户信息
     * @param page 分页对象
     * @param doctorId 医生ID
     * @return 分页结果
     */
    IPage<DoctorReview> selectReviewPageByDoctorId(Page<DoctorReview> page, @Param("doctorId") Integer doctorId);

    /**
     * 获取医生的平均评分
     */
    Float getAverageRating(@Param("doctorId") Integer doctorId);

    /**
     * 获取医生的评价数量
     */
    int getRatingCount(@Param("doctorId") Integer doctorId);

    /**
     * 获取医生满意度统计
     * @return 医生满意度统计列表
     */
    List<Map<String, Object>> getDoctorSatisfactionStats();

    /**
     * 获取科室满意度统计
     * @return 科室满意度统计列表
     */
    List<Map<String, Object>> getDepartmentStats();

    /**
     * 获取评分趋势
     * @param months 统计月数
     * @return 评分趋势数据
     */
    List<Map<String, Object>> getRatingTrend(@Param("months") int months);

    /**
     * 获取评分分布
     * @return 评分分布数据
     */
    List<Map<String, Object>> getRatingDistribution();

    /**
     * 获取评分最高的医生
     * @param limit 返回数量限制
     * @return 评分最高的医生列表
     */
    List<Map<String, Object>> getTopRatedDoctors(@Param("limit") int limit);

    /**
     * 获取用户的所有评价
     * @param userId 用户ID
     * @return 用户的评价列表
     */
    List<DoctorReview> getUserRatings(@Param("userId") Integer userId);

    /**
     * 获取医生的所有评价
     * @param doctorId 医生ID
     * @return 医生的评价列表
     */
    List<DoctorReview> getDoctorRatings(@Param("doctorId") Integer doctorId);

    /**
     * 评价管理模块 - 高级查询功能
     * 
     * @param page 分页对象
     * @param userId 用户ID（可选）
     * @param doctorId 医生ID（可选）
     * @param doctorName 医生姓名（模糊查询）
     * @param userName 用户姓名（模糊查询）
     * @param departmentId 科室ID
     * @param minRating 最低评分
     * @param maxRating 最高评分
     * @param content 评论内容关键词
     * @return 分页结果
     */
    IPage<DoctorReview> selectReviewsForManagement(
            Page<DoctorReview> page,
            @Param("userId") Long userId,
            @Param("doctorId") Integer doctorId,
            @Param("doctorName") String doctorName,
            @Param("userName") String userName,
            @Param("departmentId") Integer departmentId,
            @Param("minRating") Integer minRating,
            @Param("maxRating") Integer maxRating,
            @Param("content") String content);
} 