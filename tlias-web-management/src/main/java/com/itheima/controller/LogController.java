package com.itheima.controller;

import com.github.pagehelper.PageInfo;
import com.itheima.pojo.*;
import com.itheima.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/log")
public class LogController {

    @Autowired
    private LogService logService;
    //分页查询日志
    @GetMapping("page")
    public Result page(LogQueryParam logQueryParam){
        log.info("查询请求参数：{}", logQueryParam);
        PageResult pageResult = logService.page(logQueryParam);
        return Result.success(pageResult);
    }
}
