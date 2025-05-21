package com.sxt.controller;

import com.sxt.pojo.Result;
import com.sxt.pojo.User;
import com.sxt.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前登录用户信息
     * Returns the full User object (including password hash etc.)
     */
    @GetMapping("/me")
    //@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DOCTOR')") // Allow any logged-in role
    public Result<User> getCurrentUserProfile() {
        Long currentUserId = getCurrentUserId(); // Get ID instead of username
        if (currentUserId == null) {
            return Result.error("无法获取当前用户信息");
        }
        try {
            User userInfo = userService.getUserInfo(currentUserId);
            if (userInfo == null) {
                return Result.error("当前用户数据不存在"); // Should not happen if authenticated
            }
            return Result.success(userInfo);
        } catch (Exception e) {
            log.error("获取用户ID {} 的个人信息失败", currentUserId, e);
            return Result.error("获取个人信息失败: " + e.getMessage());
        }
    }

    /**
     * 当前用户更新自己的信息
     * Directly accepts User object
     */
    @PutMapping("/me")
    //@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DOCTOR')")
    public Result<?> updateCurrentUserProfile(@RequestBody User user) {
         Long currentUserId = getCurrentUserId();
         if (currentUserId == null) {
             return Result.error("无法获取当前用户信息");
         }
         // Ensure the ID in the body is ignored or matches the authenticated user
         user.setUserId(currentUserId);
        try {
            boolean success = userService.updateUserSelf(user);
            if (!success) {
                 // Service might return false if no changes were made
                return Result.error("更新个人信息失败或未作更改");
            }
            return Result.success(null, "个人信息更新成功");
        } catch (IllegalArgumentException e) {
            log.warn("用户 {} 更新个人信息参数错误: {}", currentUserId, e.getMessage());
            return Result.error(e.getMessage());
        } catch (SecurityException e) {
             log.warn("用户 {} 尝试更新非本人信息: {}", currentUserId, e.getMessage());
             return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("用户 {} 更新个人信息失败", currentUserId, e);
            return Result.error("更新个人信息失败: " + e.getMessage());
        }
    }

    /**
     * 当前用户修改自己的密码
     * Expects a JSON body like: {"oldPassword": "...", "newPassword": "..."}
     */
    @PutMapping("/me/password")
    //@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DOCTOR')")
    public Result<?> changeCurrentUserPassword(@RequestBody Map<String, String> passwordMap) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("无法获取当前用户信息");
        }
        String oldPassword = passwordMap.get("oldPassword");
        String newPassword = passwordMap.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return Result.error("请求体必须包含 oldPassword 和 newPassword");
        }

        try {
            boolean success = userService.changePassword(oldPassword, newPassword, currentUserId);
            if (!success) {
                 // Should be covered by exceptions below, but just in case
                return Result.error("修改密码失败");
            }
            return Result.success(null, "密码修改成功");
        } catch (IllegalArgumentException e) {
            log.warn("用户 {} 修改密码失败: {}", currentUserId, e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("用户 {} 修改密码时发生错误", currentUserId, e);
            return Result.error("修改密码失败: " + e.getMessage());
        }
    }

    /**
     * 当前用户注销自己的账号
     */
    @DeleteMapping("/me")
    //@PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DOCTOR')")
    public Result<?> deleteCurrentUserAccount() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("无法获取当前用户信息");
        }
        try {
             // Use deleteUserById now
            boolean success = userService.deleteUserById(currentUserId);
            if (!success) {
                return Result.error("注销账号失败，可能用户已被删除");
            }
            SecurityContextHolder.clearContext(); // Clear context after successful deletion
            return Result.success(null, "账号已成功注销");
        } catch (Exception e) {
            log.error("用户 {} 注销账号失败", currentUserId, e);
            return Result.error("注销账号失败: " + e.getMessage());
        }
    }

    /**
     * Helper method to get the ID of the currently authenticated user.
     * @return User ID (Long) or null if not authenticated.
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = (authentication != null) ? authentication.getPrincipal() : null;

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            User user = userService.findByUsername(username);
            return (user != null) ? user.getUserId() : null;
        } else if (principal instanceof String) {
            User user = userService.findByUsername((String) principal);
            return (user != null) ? user.getUserId() : null;
        } else if (principal instanceof User) { // If our User object is the principal
            return ((User) principal).getUserId();
        }

        log.warn("无法从 SecurityContextHolder 获取当前认证用户ID");
        return null;
    }
} 