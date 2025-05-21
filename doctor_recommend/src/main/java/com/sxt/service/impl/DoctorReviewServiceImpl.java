package com.sxt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sxt.mapper.DoctorMapper;
import com.sxt.mapper.DoctorReviewMapper;
import com.sxt.mapper.UserMapper;
import com.sxt.pojo.Doctor;
import com.sxt.pojo.DoctorReview;
import com.sxt.pojo.User;
import com.sxt.service.DoctorReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
public class DoctorReviewServiceImpl extends ServiceImpl<DoctorReviewMapper, DoctorReview> implements DoctorReviewService {

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private DoctorReviewMapper doctorReviewMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public boolean addReviewAndUpdateDoctorRating(DoctorReview review) {
        if (review == null || review.getDoctorId() == null || review.getUserId() == null
                || review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("评论信息无效或评分超出范围 (1-5)");
        }

        Doctor doctor = doctorMapper.selectById(review.getDoctorId());
        if (doctor == null) {
            throw new IllegalArgumentException("评论的医生不存在, ID: " + review.getDoctorId());
        }

        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("user_id", review.getUserId());
        User user = userMapper.selectOne(userWrapper);
        if (user == null) {
            throw new IllegalArgumentException("评论用户不存在, ID: " + review.getUserId());
        }

        review.setCreatedAt(new Date());
        boolean reviewSaved = this.save(review);
        if (!reviewSaved) {
            log.error("保存评论失败, review data: {}", review);
            throw new RuntimeException("保存评论失败");
        }

        try {
            int oldRatingCount = doctor.getRatingCount() != null ? doctor.getRatingCount() : 0;
            float oldAverageRating = doctor.getAverageRating() != null ? doctor.getAverageRating() : 0.0f;
            int newRating = review.getRating();
            int newRatingCount = oldRatingCount + 1;
            float newAverageRating = ((oldAverageRating * oldRatingCount) + newRating) / (float) newRatingCount;

            Doctor doctorToUpdate = new Doctor();
            doctorToUpdate.setDoctorId(doctor.getDoctorId());
            doctorToUpdate.setRatingCount(newRatingCount);
            doctorToUpdate.setAverageRating(Math.round(newAverageRating * 10.0f) / 10.0f);
            
            int updatedRows = doctorMapper.updateById(doctorToUpdate);
            if (updatedRows == 0) {
                log.error("更新医生评分失败, doctorId: {}", doctor.getDoctorId());
                throw new RuntimeException("更新医生评分失败");
            }
            
            log.info("成功添加评论并更新医生评分, doctorId: {}, newAverageRating: {}, newRatingCount: {}",
                    doctor.getDoctorId(), doctorToUpdate.getAverageRating(), newRatingCount);
            return true;
        } catch (Exception e) {
            log.error("更新医生评分时发生异常, doctorId: {}, reviewId: {}", doctor.getDoctorId(), review.getReviewId(), e);
            throw new RuntimeException("更新医生评分时发生错误", e);
        }
    }

    @Override
    public IPage<DoctorReview> getReviewsByDoctorId(Integer doctorId, Page<DoctorReview> page) {
        if (doctorId == null) {
            throw new IllegalArgumentException("医生ID不能为空");
        }
        log.info("分页查询医生评论, doctorId: {}, page: {}, size: {}", doctorId, page.getCurrent(), page.getSize());
        return doctorReviewMapper.selectReviewPageByDoctorId(page, doctorId);
    }

    @Override
    public DoctorReview getById(Long id) {
        if (id == null) {
            return null;
        }
        return baseMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean removeReviewAndUpdateDoctorRating(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("评论ID不能为空");
        }
        
        DoctorReview review = baseMapper.selectById(id);
        if (review == null) {
            log.warn("尝试删除不存在的评论: {}", id);
            return false;
        }
        
        Integer doctorId = review.getDoctorId();
        if (doctorId == null) {
            log.warn("评论记录缺少医生ID: {}", id);
            throw new IllegalStateException("评论记录数据异常，缺少医生ID");
        }
        
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            log.warn("评论对应的医生不存在, doctorId: {}", doctorId);
            throw new IllegalStateException("评论对应的医生不存在");
        }
        
        int deletedRows = baseMapper.deleteById(id);
        if (deletedRows == 0) {
            log.warn("删除评论失败, id: {}", id);
            return false;
        }
        
        try {
            int oldRatingCount = doctor.getRatingCount() != null ? doctor.getRatingCount() : 0;
            float oldAverageRating = doctor.getAverageRating() != null ? doctor.getAverageRating() : 0.0f;
            
            if (oldRatingCount <= 1) {
                Doctor doctorToUpdate = new Doctor();
                doctorToUpdate.setDoctorId(doctor.getDoctorId());
                doctorToUpdate.setRatingCount(0);
                doctorToUpdate.setAverageRating(0.0f);
                doctorMapper.updateById(doctorToUpdate);
            } else {
                int ratingToRemove = review.getRating();
                int newRatingCount = oldRatingCount - 1;
                
                float totalOldRating = oldAverageRating * oldRatingCount;
                float newTotalRating = totalOldRating - ratingToRemove;
                float newAverageRating = newTotalRating / newRatingCount;
                
                Doctor doctorToUpdate = new Doctor();
                doctorToUpdate.setDoctorId(doctor.getDoctorId());
                doctorToUpdate.setRatingCount(newRatingCount);
                doctorToUpdate.setAverageRating(Math.round(newAverageRating * 10.0f) / 10.0f);
                
                int updatedRows = doctorMapper.updateById(doctorToUpdate);
                if (updatedRows == 0) {
                    log.error("更新医生评分失败, doctorId: {}", doctor.getDoctorId());
                    throw new RuntimeException("更新医生评分失败");
                }
            }
            
            log.info("成功删除评论并更新医生评分, id: {}, doctorId: {}", id, doctorId);
            return true;
        } catch (Exception e) {
            log.error("更新医生评分时发生异常, doctorId: {}, id: {}", doctorId, id, e);
            throw new RuntimeException("删除评论后更新医生评分失败", e);
        }
    }

    @Override
    public IPage<DoctorReview> getReviewsForManagement(
            Long userId,
            Integer doctorId,
            String doctorName,
            String userName,
            Integer departmentId,
            Integer minRating,
            Integer maxRating,
            String content,
            Page<DoctorReview> page) {
        
        log.info("评价管理查询: 用户ID={}, 医生ID={}, 医生姓名={}, 用户姓名={}, 科室ID={}, 评分范围={}~{}, 评论内容={}, 页码={}, 每页大小={}", 
                userId, doctorId, doctorName, userName, departmentId, minRating, maxRating, content, page.getCurrent(), page.getSize());
        
        try {
            return doctorReviewMapper.selectReviewsForManagement(
                    page, userId, doctorId, doctorName, userName, departmentId, minRating, maxRating, content);
        } catch (Exception e) {
            log.error("评价管理查询出错: {}", e.getMessage(), e);
            throw new RuntimeException("评价管理查询失败", e);
        }
    }

    @Override
    @Transactional
    public boolean deleteReviewsByDoctorId(Integer doctorId) {
        if (doctorId == null) {
            log.warn("删除评价失败：医生ID为空");
            return false;
        }
        
        log.info("删除医生(ID: {})的所有评价", doctorId);
        
        try {
            QueryWrapper<DoctorReview> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("doctor_id", doctorId);
            
            int count = baseMapper.delete(queryWrapper);
            log.info("成功删除医生(ID: {})的评价数量: {}", doctorId, count);
            
            Doctor doctor = doctorMapper.selectById(doctorId);
            if (doctor != null) {
                doctor.setRatingCount(0);
                doctor.setAverageRating(0.0f);
                doctorMapper.updateById(doctor);
                log.info("重置医生(ID: {})的评分统计", doctorId);
            }
            
            return true;
        } catch (Exception e) {
            log.error("删除医生评价时发生错误, doctorId: {}", doctorId, e);
            throw new RuntimeException("删除医生评价失败", e);
        }
    }
} 