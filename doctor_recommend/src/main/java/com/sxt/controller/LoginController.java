package com.sxt.controller;

import com.sxt.pojo.Result;
import com.sxt.pojo.User;
import com.sxt.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping
public class LoginController {
    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        try {
            Map<String, Object> result = loginService.login(user.getUsername(), user.getPassword());
            if (result == null) {
                return Result.error("登录失败，用户名或密码错误");
            }
            return Result.success(result, "登录成功");
        } catch (Exception e) {
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    @PostMapping("/register")
    public Result register(@Validated @RequestBody User user, BindingResult bindingResult) {
        try {
            if (bindingResult.hasErrors()) {
                return Result.error(bindingResult.getFieldErrors().get(0).getDefaultMessage());
            }
            boolean success = loginService.register(user);
            if (!success) {
                return Result.error("注册失败，用户名可能已存在");
            }
            return Result.success(null, "注册成功");
        } catch (Exception e) {
            return Result.error("注册失败：" + e.getMessage());
        }
    }

    @GetMapping("/register/check-username")
    public Result checkUsername(@RequestParam String username) {
        try {
            boolean exists = loginService.checkUsername(username);
            return Result.success(exists, "success");
        } catch (Exception e) {
            return Result.error("检查用户名失败：" + e.getMessage());
        }
    }
}
