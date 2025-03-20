//package com.itheima.controller;
//
//import com.itheima.pojo.Emp;
//import com.itheima.pojo.EmpQueryParam;
//import com.itheima.pojo.PageResult;
//import com.itheima.pojo.Result;
//import com.itheima.service.EmpService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.time.LocalDate;
//
//@RestController
//@Slf4j
//@RequestMapping("/emps")
////接口文档中：请求路径“/emps”，该接口用于员工列表的数据查询
////后面gemapping就可以不需要加入/emps
//public class EmpController {
//    //接收参数（分页）
//    @Autowired //需要调用service,自动注入
//    private EmpService empService;
//    @GetMapping
//    /*public Result page(@RequestParam(defaultValue ="1" ) Integer page,//本注解设置默认值
//                       @RequestParam (defaultValue = "10") Integer pageSize,
//                       //根据接口文档，添加对应的参数,参数的类型看类声明
//                       String name, Integer gender,
//                       //时间，添加注释，通过sping中的此注解，指定前端传过来的数据格式
//                       @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
//                       @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end)
//                        //新建一个类进行封装以上参数*/
//    public Result page(EmpQueryParam empQueryParam)
//    {
//        //log.info("分页查询：{},{},{},{},{},{}",page,pageSize,name,gender,begin,end);
//        log.info("分页查询：{}",empQueryParam);
//        PageResult<Emp> pageResult = empService.page(empQueryParam);
//        //log.info("分页查询：page={},pageSize={}",page,pageSize);
//        //PageResult<Emp> pageResult = empService.page(page, pageSize,name, gender, begin, end);
//        return Result.success(pageResult);
//    }
//    //调用service，进行分页查询，获取PageResult
//    //响应结果
//}



package com.itheima.controller;

import com.itheima.anno.LogOperation;
import com.itheima.pojo.Emp;
import com.itheima.pojo.EmpQueryParam;
import com.itheima.pojo.PageResult;
import com.itheima.pojo.Result;
import com.itheima.service.EmpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 员工管理
 */
//@Slf4j
//@RequestMapping("/emps")
//@RestController
//public class EmpController {
//
//    @Autowired
//    private EmpService empService;
//
//    @GetMapping
//    public Result page(@RequestParam(defaultValue = "1") Integer page ,
//                       @RequestParam(defaultValue = "10") Integer pageSize){
//        log.info("查询员工信息, page={}, pageSize={}", page, pageSize);
//        PageResult pageResult = empService.page(page, pageSize);
//        return Result.success(pageResult);
//    }
//
//}


@Slf4j
@RestController
@RequestMapping("/emps")
public class EmpController {

    @Autowired
    private EmpService empService;

    @GetMapping
    //@LogOperation
    public Result page(EmpQueryParam empQueryParam) {
        log.info("查询请求参数： {}",empQueryParam );
        PageResult pageResult = empService.page(empQueryParam);
        return Result.success(pageResult);
    }

    //添加员工
    // @RequestBody注解，表示将请求体中的json数据封装到Emp对象中
    @PostMapping
    public Result save(@RequestBody Emp emp)
    {
       log.info("新增员工:{}",emp);
       empService.save(emp);
       return Result.success();
    }

//    @DeleteMapping
//    public Result delete(Integer[] ids){
//        log.info("批量删除员工:ids={}", Arrays.asList(ids));
//        return Result.success();
//    }

    //通过集合来接收将其封装到<list>集合中
    //需要在集合前面加@RequestParam注解
    // @RequestParam 注解表示将请求参数名与形参变量名相同，
    @DeleteMapping
    public Result delete(@RequestParam List<Integer> ids){
        log.info("批量删除员工:ids={}",ids);
        empService.deleteByIds(ids);
        return Result.success();
    }
    //查询所有员工
    @GetMapping("/list")
    public Result list() {
        log.info("查询所有员工");
        List<Emp> list = empService.findAll();
        return Result.success(list);
    }


    //查询回显
    @GetMapping("/{id}")
    public Result getInfo(@PathVariable Integer id) {
        log.info("查询回显：{}",id);
        Emp emp = empService.getInfo(id);
        return Result.success(emp);
    }
    //修改员工信息
    @PutMapping
    //属于请求映射注解的一种，专用于将HTTP的put请求映射到某个具体的方法上
    //通常用于更新数据的场景
    public Result update(@RequestBody Emp emp){
        log.info("修改员工信息:{}",emp);
        empService.update(emp);
        return Result.success();
    }


}

