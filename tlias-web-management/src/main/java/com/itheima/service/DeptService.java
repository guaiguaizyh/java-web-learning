package com.itheima.service;

import com.itheima.pojo.Dept;

import java.util.List;

public interface DeptService {
    //查询所有部门，返回一个集合
    public List<Dept> findAll();
    //删除部门，不需要返回数据
    void deleteById(Integer deptId);
    //增加部门
    void save(Dept dept);

    Dept findById(Integer id);

    void update(Dept dept);
}
