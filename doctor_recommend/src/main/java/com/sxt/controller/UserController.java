package com.sxt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sxt.pojo.Result;
import com.sxt.pojo.User;
import com.sxt.pojo.vo.UserVO;
import com.sxt.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理接口
 * 包含管理员的用户管理接口和普通用户的个人信息管理接口
 */
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前登录用户的个人信息
     */
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getUserByUsername(userDetails.getUsername());
            if (user == null) {
                return Result.error("用户不存在");
            }
            UserVO userVO = userService.convertToUserVO(user);
            return Result.success(userVO);
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新当前登录用户的个人信息
     */
    @PutMapping("/me")
    public Result<?> updateCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserVO userVO) {
        try {
            User user = userService.getUserByUsername(userDetails.getUsername());
            if (user == null) {
                return Result.error("用户不存在");
            }
            // 确保不能修改其他用户的信息
            userVO.setUserId(user.getUserId());
            boolean success = userService.updateUserProfile(userVO);
            if (!success) {
                return Result.error("更新用户信息失败或未做更改");
            }
            return Result.success(null, "个人信息更新成功");
        } catch (IllegalArgumentException e) {
            log.warn("更新用户参数错误: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新用户信息失败", e);
            return Result.error("更新用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户列表（分页，管理员权限）
     * 返回 UserVO 对象列表（不含敏感信息）
     */
    @GetMapping
    public Result<IPage<UserVO>> getUserList(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String gender) {
        try {
            // 参数校验
            if (minAge != null && maxAge != null && minAge > maxAge) {
                return Result.error("最小年龄不能大于最大年龄");
            }
            if (minAge != null && minAge < 0) {
                return Result.error("年龄不能小于0");
            }
            if (maxAge != null && maxAge > 150) {
                return Result.error("年龄不能大于150");
            }
            if (gender != null && !gender.matches("^[男女]$")) {
                return Result.error("性别只能是'男'或'女'");
            }

            Page<User> pageRequest = new Page<>(page, size);
            IPage<UserVO> userPage = userService.getUserVOListPage(pageRequest, username, phone, minAge, maxAge, gender);
            return Result.success(userPage);
        } catch (Exception e) {
            log.error("管理员获取用户列表失败", e);
            return Result.error("获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取用户信息（管理员权限）
     * 返回 UserVO 对象（不含敏感信息）
     */
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            UserVO userVO = userService.convertToUserVO(user);
            return Result.success(userVO);
        } catch (Exception e) {
            log.error("管理员获取用户信息失败, ID: {}", userId, e);
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户信息（管理员权限）
     */
    @PutMapping("/{userId}")
    public Result<?> updateUserByAdmin(@PathVariable Long userId, @RequestBody User user) {
        user.setUserId(userId);
        try {
            boolean success = userService.updateUserByAdmin(user);
            if (!success) {
                return Result.error("更新用户信息失败或未做更改");
            }
            return Result.success(null, "用户信息更新成功");
        } catch (IllegalArgumentException e) {
            log.warn("更新用户参数错误, ID: {}, Error: {}", userId, e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新用户信息失败, ID: {}", userId, e);
            return Result.error("更新用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户（管理员权限）
     */
    @DeleteMapping("/{userId}")
    public Result<?> deleteUser(@PathVariable Long userId) {
        try {
            boolean success = userService.deleteUserById(userId);
            if (!success) {
                return Result.error("删除用户失败，可能用户不存在");
            }
            return Result.success(null, "用户删除成功");
        } catch (IllegalArgumentException e) {
            log.warn("删除用户参数错误, ID: {}, Error: {}", userId, e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("删除用户失败, ID: {}", userId, e);
            return Result.error("删除用户失败: " + e.getMessage());
        }
    }

    /**
     * 添加新用户（管理员权限）
     */
    @PostMapping
    public ResponseEntity<Result<UserVO>> addUser(@RequestBody User user) {
        user.setUserId(null);
        try {
            boolean success = userService.addUser(user);
            if (success && user.getUserId() != null) {
                User createdUser = userService.getUserById(user.getUserId());
                UserVO userVO = userService.convertToUserVO(createdUser);
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Result.success(userVO, "用户添加成功"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("添加用户失败"));
            }
        } catch (IllegalArgumentException e) {
            log.warn("管理员添加用户失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        } catch (Exception e) {
            log.error("管理员添加用户时发生内部错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("添加用户时发生内部错误"));
        }
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<?> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> passwordData) {
        
        try {
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");
            
            if (oldPassword == null || newPassword == null) {
                return Result.error("旧密码和新密码不能为空");
            }
            
            User user = userService.getUserByUsername(userDetails.getUsername());
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            boolean success = userService.changePassword(oldPassword, newPassword, user.getUserId());
            if (success) {
                return Result.success(null, "密码修改成功");
            } else {
                return Result.error("密码修改失败");
            }
        } catch (IllegalArgumentException e) {
            log.warn("修改密码参数错误: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("修改密码发生错误", e);
            return Result.error("修改密码失败: " + e.getMessage());
        }
    }
} 