package com.itheima.controller;

import com.itheima.anno.LogOperation;
import com.itheima.pojo.*;
import com.itheima.service.ClazzService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/clazzs")
public class ClazzController {
    @Autowired
    private ClazzService clazzService;


    @GetMapping//根据接口文件get路径，分页查询班级信息
    public Result page(ClazzQueryParam clazzQueryParam) {
        log.info("分页查询班级信息，参数：{}", clazzQueryParam);
        PageResult pageResult=clazzService.page(clazzQueryParam);
        return Result.success(pageResult);
    }

    @PostMapping
    public Result save(@RequestBody Clazz clazz)
    {
        log.info("新增列表:{}",clazz);
        clazzService.insert(clazz);
        Clazz fullClazz = clazzService.getById(clazz.getId());
        return Result.success(fullClazz);
    }
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) //@PathVariable注解表示从请求路径中获取参数，
    {
        //System.out.println("删除部门：" + id);
        clazzService.deleteById(id);//deleteById方法在服务层中实现
        return Result.success();//deleteById具体操作在Mapper中编写
    }

    //修改操作
    //查询回显
    @GetMapping("/{id}")
    public Result getInfo(@PathVariable Integer id) {
        log.info("查询回显：{}",id);
        Clazz clazz = clazzService.getById(id);
        return Result.success(clazz);
    }
    //修改员工信息
    @PutMapping
    //属于请求映射注解的一种，专用于将HTTP的put请求映射到某个具体的方法上
    //通常用于更新数据的场景
    public Result update(@RequestBody Clazz clazz){
        log.info("修改列表信息:{}",clazz);
        clazzService.update(clazz);
        Clazz fullClazz = clazzService.getById(clazz.getId());
        return Result.success(fullClazz);
    }

    //查询全部班级信息
    @GetMapping("/list")
    public Result list() {
        log.info("查询全部班级信息");
        List<Clazz> list = clazzService.findALL();
        return Result.success(list);
    }
}
