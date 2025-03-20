package com.itheima.controller;

import com.itheima.pojo.*;
import com.itheima.service.StuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/students")
public class StuController {
    @Autowired
    private StuService stuService;
    //分页学院列表查询
    @GetMapping//根据接口文件get路径，分页查询班级信息
    public Result page(StuQueryParam stuQueryParam) {
        log.info("分页查询学员信息，参数：{}", stuQueryParam);
        PageResult pageResult= stuService.page(stuQueryParam);
        return Result.success(pageResult);
    }
    //批量删除
    @DeleteMapping("/{ids}")
    public Result delete(@PathVariable List<Integer> ids){
        log.info("批量删除员工:ids={}",ids);
        stuService.deleteByIds(ids);
        return Result.success();
    }
    //添加
    @PostMapping
    public Result save(@RequestBody Stu stu)
    {
        log.info("新增学员:{}",stu);
        stuService.save(stu);
        return Result.success();
    }
    //修改
    //数据回显
    @GetMapping("/{id}")
    public Result getInfo(@PathVariable Integer id) {
        log.info("查询回显：{}",id);
        Stu stu = stuService.getInfo(id);
        return Result.success(stu);
    }
    @PutMapping
    public Result update(@RequestBody Stu stu)
    {
        log.info("修改学员:{}",stu);
        stuService.update(stu);
        return Result.success();
    }

    //违纪处理
    @PutMapping("/violation/{id}/{score}")
    public Result handleViolation(@PathVariable Integer id, @PathVariable Integer score) {
        log.info("处理学员违纪: 学员ID = {}, 违纪分数 = {}", id, score);
        stuService.handleViolation(id, score);
        return Result.success();
    }

}
