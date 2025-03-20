package com.itheima.service;

import com.itheima.pojo.Clazz;
import com.itheima.pojo.ClazzQueryParam;
import com.itheima.pojo.PageResult;

import java.util.List;


public interface ClazzService {
    //分页查询班级信息
    PageResult page(ClazzQueryParam clazzQueryParam);
    //添加员工
    void insert(Clazz clazz);

    //删除员工
    void deleteById(Integer id);
    //查询回显
    Clazz getById(Integer id);
    //修改数据
    void update(Clazz clazz);
    //查询班级全部信息
    List<Clazz> findALL();
}
