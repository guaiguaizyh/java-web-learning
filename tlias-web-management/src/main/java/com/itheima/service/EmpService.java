//package com.itheima.service;
//
//import com.itheima.pojo.Emp;
//import com.itheima.pojo.EmpQueryParam;
//import com.itheima.pojo.PageResult;
//
//import java.time.LocalDate;
//
//
//public interface EmpService{
//
//    /*分页查询
//    * @param page:页码
//    * @param pageSize:每页展示的记录数
//    */
//   // PageResult<Emp> page(Integer page, Integer pageSize);
//    //PageResult<Emp> page(Integer page, Integer pageSize, String name, Integer gender, LocalDate begin, LocalDate end);
//
//    PageResult<Emp> page(EmpQueryParam empQueryParam);
//    //根据接口文档，添加对应的参数,参数的类型看类声明
//}





package com.itheima.service;

import com.itheima.pojo.Emp;
import com.itheima.pojo.EmpQueryParam;
import com.itheima.pojo.LoginInfo;
import com.itheima.pojo.PageResult;

import java.util.List;

//public interface EmpService {
//    /**
//     * 分页查询
//     * @param page 页码
//     * @param pageSize 每页记录数
//     */
//    PageResult page(Integer page, Integer pageSize);
//}
public interface EmpService {
    /**
     * 分页查询
     */
    //PageResult page(Integer page, Integer pageSize, String name, Integer gender, LocalDate begin, LocalDate end);

    PageResult page(EmpQueryParam empQueryParam);
    //添加员工
     void save(Emp emp);
    //删除员工
    void deleteByIds(List<Integer> ids);
    //查询回显
    Emp getInfo(Integer id);
    //修改数据
    void update(Emp emp);
    //登陆
    LoginInfo login(Emp emp);

    //查询所有员工信息
    List<Emp> findAll();
}
