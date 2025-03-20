package com.itheima.controller;

import com.itheima.anno.LogOperation;
import com.itheima.pojo.Dept;
import com.itheima.pojo.Result;
import com.itheima.service.DeptService;
import com.itheima.service.EmpService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 部门管理控制器
 */
@Slf4j
@RestController
//相当于加了两个注解，
// @Controller + @ResponseBody：将方法的返回值直接作为响应值响应给前端 不需要转换为json格式
public class DeptController {

    //定义一个日志记录对象
    //private static final Logger log = org.slf4j.LoggerFactory.getLogger(DeptController.class);
    //也可以直接加入slf4j的注解，直接使用log对象记录日志


    @Autowired//添加一个注解，自动注入
    private DeptService deptService;
    @Autowired
    private EmpService empService;

    // 查询所有部门数据
    //返回list到Result中,封装起来
    //@RequestMapping(value = "/depts",method = RequestMethod.GET)
//    @LogOperation//在查询操作上面添加自定义注解
    @GetMapping("/depts")
    @LogOperation
    //接口文档显示：请求路径“/depts"，该接口用于部门列表的数据查询
    //请求参数：无,请求方式：GET，响应数据：参数格式：application/json
    //根据接口文档，接受请求，调用service层查询数据，响应数据
    public Result list() {
        List<Dept> deptList = deptService.findAll();
        // 查询返回的数据封装到Result中
        return Result.success(deptList);
        //见Result类中的封装方法
    }


    //删除部门,需要接受前端传递的请求参数，根据id主键删除部门
    //获取参数的方式有三种
    //方案一：通过原始的HttpServletRequest对象获取参数,麻烦，需要手动进行类型转换
    //方案二：通过@RequestParam注解获取参数,
//    public Result delete(@RequestParam("id") Integer deptId) {
//        System.out.println("删除部门：" + deptId);
//        return Result.success();
//    }
    //@RequestParam注释的value属性，前端传递参数名与服务端方法形参保持一致。
    //@RequestParam注解时，本参数请求时必须传递，否则报错。required属性默认为true，
    // 代表该参数必须传递，如果不传递将报错。 如果参数可选，可以将属性设置为false。
    //方式三：如果请求参数名与形参变量名相同，直接定义方法形参即可接受，可省略@RequestParam注解
    @DeleteMapping("/depts")
    public Result delete(Integer id) {
        //System.out.println("删除部门：" + id);
        deptService.deleteById(id);//deleteById方法在服务层中实现
        return Result.success();//deleteById具体操作在Mapper中编写
    }

    //添加部门
    @PostMapping("/depts")
    //@RequestBody注解表示将请求体中的json数据封装到dept对象中
    //Json数据的键名与方法形参对象的属性名相同，
    // 并需要使用@RequestBody注解，否则无法封装。
    public Result save(@RequestBody Dept dept) {
        //System.out.println("添加部门：" + dept);
        log.info("添加部门:"+dept);
        deptService.save(dept);
        return Result.success();
    }

    //修改部门名称
    //查询回显
    @GetMapping("/depts/{id}")
    //@LogOperation
    //@PathVariable注解表示从请求路径中获取参数，
    public Result findById(@PathVariable Integer id) {
        //System.out.println("查询回显：" + id);
        log.info("查询数据：{}",id);
        Dept dept = deptService.findById(id);
        return Result.success(dept);
    }
    //修改数据
    @PutMapping("/depts")
    public Result update(@RequestBody Dept dept) {
        System.out.println("修改部门：" + dept);
        deptService.update(dept);
        return Result.success();
    }


}



