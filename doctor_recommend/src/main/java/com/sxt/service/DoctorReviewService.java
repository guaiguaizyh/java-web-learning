package com.sxt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sxt.pojo.DoctorReview;

public interface DoctorReviewService {

    /**
     * 添加一条医生评论，并更新医生的平均评分。
     *
     * @param review 包含评论信息的对象 (需要设置 doctorId, userId, rating, comment, isAnonymous)
     * @return 是否添加成功
     * @throws IllegalArgumentException 如果评分无效或医生不存在
     */
    boolean addReviewAndUpdateDoctorRating(DoctorReview review);

    /**
     * 分页获取指定医生的评论列表（包含评论者用户名）。
     *
     * @param doctorId 医生ID
     * @param page     分页参数对象
     * @return 评论的分页列表
     */
    IPage<DoctorReview> getReviewsByDoctorId(Integer doctorId, Page<DoctorReview> page);
    
    /**
     * 根据ID获取评论
     * @param id 评论ID
     * @return 评论对象，不存在则返回null
     */
    DoctorReview getById(Long id);
    
    /**
     * 删除评论并更新医生评分
     * @param id 评论ID
     * @return 是否成功删除
     */
    boolean removeReviewAndUpdateDoctorRating(Long id);

    /**
     * 评价管理模块 - 高级查询功能
     * 
     * @param userId 用户ID（可选）
     * @param doctorId 医生ID（可选）
     * @param doctorName 医生姓名（模糊查询）
     * @param userName 用户姓名（模糊查询）
     * @param departmentId 科室ID
     * @param minRating 最低评分
     * @param maxRating 最高评分
     * @param content 评论内容关键词
     * @param page 分页参数
     * @return 符合条件的评价分页结果
     */
    IPage<DoctorReview> getReviewsForManagement(
            Long userId,
            Integer doctorId,
            String doctorName,
            String userName,
            Integer departmentId,
            Integer minRating,
            Integer maxRating,
            String content,
            Page<DoctorReview> page);

    /**
     * 删除指定医生的所有评价
     * @param doctorId 医生ID
     * @return 是否成功删除
     */
    boolean deleteReviewsByDoctorId(Integer doctorId);
} 