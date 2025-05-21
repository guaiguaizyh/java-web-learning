package com.sxt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sxt.pojo.DoctorReview;
import com.sxt.pojo.Result;
import com.sxt.pojo.User;
import com.sxt.service.DoctorReviewService;
import com.sxt.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/reviews")
@CrossOrigin(origins = "*")
public class DoctorReviewController {

    @Autowired
    private DoctorReviewService doctorReviewService;

    @Autowired
    private UserService userService;

    /**
     * 添加医生评论
     * 需要用户登录
     * @param review 包含 doctorId, rating, comment, isAnonymous 的请求体
     * @return 操作结果
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Result<?>> addDoctorReview(@RequestBody DoctorReview review) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error("无法获取当前用户信息，请登录"));
        }

        review.setUserId(currentUserId);
        review.setReviewId(null);
        review.setCreatedAt(null);

        try {
            boolean success = doctorReviewService.addReviewAndUpdateDoctorRating(review);
            if (success) {
                return ResponseEntity.status(HttpStatus.CREATED).body(Result.success(null, "评论提交成功"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.error("提交评论失败"));
            }
        } catch (IllegalArgumentException e) {
            log.warn("添加评论失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        } catch (Exception e) {
            log.error("添加评论时发生内部错误, userId: {}, doctorId: {}", review.getUserId(), review.getDoctorId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.error("提交评论时发生内部错误"));
        }
    }

    /**
     * 获取指定医生的评论列表（分页）
     * @param doctorId 医生ID
     * @param page     当前页码
     * @param size     每页大小
     * @return 评论分页结果
     */
    @GetMapping("/doctor/{doctorId}")
    public Result<IPage<DoctorReview>> getDoctorReviews(
            @PathVariable Integer doctorId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<DoctorReview> pageRequest = new Page<>(page, size);
            IPage<DoctorReview> reviewPage = doctorReviewService.getReviewsByDoctorId(doctorId, pageRequest);
            return Result.success(reviewPage);
        } catch (IllegalArgumentException e) {
            log.warn("查询医生评论失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询医生 {} 的评论时发生错误", doctorId, e);
            return Result.error("查询评论失败: " + e.getMessage());
        }
    }

    /**
     * 删除评论
     * 用户只能删除自己的评论，管理员可以删除任何评论
     * @param id 评论ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Result<?> deleteReview(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        
        try {
            DoctorReview review = doctorReviewService.getById(id);
            if (review == null) {
                return Result.error("评论不存在");
            }
            
            if (currentUserId == 0L || currentUserId.equals(review.getUserId())) {
                boolean success = doctorReviewService.removeReviewAndUpdateDoctorRating(id);
                if (success) {
                    return Result.success(null, "评论删除成功");
                } else {
                    return Result.error("删除评论失败");
                }
            } else {
                return Result.error("您没有权限删除此评论");
            }
        } catch (Exception e) {
            log.error("删除评论时发生错误, reviewId: {}", id, e);
            return Result.error("删除评论失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户的ID
     * @return 用户ID，未登录则返回null
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            if ("admin".equals(username)) {
                log.info("当前用户是管理员: {}", username);
                return 0L;
            }
            
            User user = userService.findByUsername(username);
            if (user != null) {
                return user.getUserId();
            } else {
                log.warn("无法找到用户: {}", username);
                return null;
            }
        }
        return null;
    }

    /**
     * 评价管理模块 - 高级查询功能
     * 支持医生姓名模糊查询、用户姓名模糊查询、科室查询、评分范围筛选
     * 
     * @param userId 用户ID（可选）
     * @param doctorId 医生ID（可选）
     * @param doctorName 医生姓名（模糊查询）
     * @param userName 用户姓名（模糊查询）
     * @param departmentId 科室ID
     * @param minRating 最低评分
     * @param maxRating 最高评分
     * @param content 评论内容关键词
     * @param page 当前页码（默认：1）
     * @param size 每页大小（默认：10）
     * @return 符合条件的评价分页结果
     */
    @GetMapping("/manage")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<DoctorReview>> manageReviews(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer doctorId,
            @RequestParam(required = false) String doctorName,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) Integer maxRating,
            @RequestParam(required = false) String content,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            if (minRating != null && (minRating < 1 || minRating > 5)) {
                return Result.error("最低评分必须在1-5之间");
            }
            if (maxRating != null && (maxRating < 1 || maxRating > 5)) {
                return Result.error("最高评分必须在1-5之间");
            }
            if (minRating != null && maxRating != null && minRating > maxRating) {
                return Result.error("最低评分不能大于最高评分");
            }
            
            log.info("评价管理-高级查询: 用户ID={}, 医生ID={}, 医生姓名={}, 用户姓名={}, 科室ID={}, 评分范围={}~{}, 评论内容={}, 页码={}, 每页大小={}", 
                    userId, doctorId, doctorName, userName, departmentId, minRating, maxRating, content, page, size);
            
            Page<DoctorReview> pageRequest = new Page<>(page, size);
            IPage<DoctorReview> reviewPage = doctorReviewService.getReviewsForManagement(
                    userId, doctorId, doctorName, userName, departmentId, minRating, maxRating, content, pageRequest);
            
            return Result.success(reviewPage);
        } catch (Exception e) {
            log.error("评价管理查询时发生错误", e);
            return Result.error("评价管理查询失败: " + e.getMessage());
        }
    }
}