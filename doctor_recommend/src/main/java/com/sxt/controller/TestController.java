package com.sxt.controller;

import com.sxt.pojo.Result;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 测试控制器
 * 用于验证安全配置和API请求
 */
@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    /**
     * 简单的GET请求测试
     * @return 测试结果
     */
    @GetMapping("/hello")
    public Result<String> hello() {
        log.info("测试GET请求");
        return Result.success("GET请求成功");
    }

    /**
     * 测试POST请求
     * @param requestBody 请求体
     * @return 测试结果
     */
    @PostMapping("/echo")
    public Result<Map<String, Object>> testPost(@RequestBody Map<String, Object> requestBody) {
        log.info("接收到POST请求数据: {}", requestBody);
        return Result.success(requestBody);
    }

    /**
     * 模拟AI直接推荐接口的简化版本
     * @param requestBody 请求体
     * @return 测试结果
     */
    @PostMapping("/mock-ai-direct")
    public Result<String> mockAiDirect(@RequestBody Map<String, Object> requestBody) {
        log.info("模拟AI直接推荐接口请求: {}", requestBody);
        String description = (String) requestBody.getOrDefault("description", "");
        return Result.success("成功接收到症状描述: " + description);
    }
} 