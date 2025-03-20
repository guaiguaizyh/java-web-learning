package com.itheima.service.impl;


import com.itheima.mapper.DeptMapper;
import com.itheima.pojo.Dept;
import com.itheima.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/*执行类*/
@Service
public class DeptServiceImpl implements DeptService {
    @Autowired//注入依赖
    private DeptMapper deptMapper;
    public List<Dept> findAll()
    {
        return deptMapper.findAll();
    }
    //添加deleteById方法
    public void deleteById(Integer id)
    {
        deptMapper.deleteById(id);
    }

    //添加save方法
    public void save(Dept dept) {
        //补齐基本属性，自动生成的更新时间
        dept.setCreateTime(LocalDateTime.now());
        dept.setUpdateTime(LocalDateTime.now());
        //保存部门
        deptMapper.insert(dept);
    }
    //修改部门名称，查询回显
    public Dept findById(Integer id) {
        return deptMapper.findById(id);
    }
    public void update(Dept dept) {
        //补齐基本属性，自动生成的更新时间
        dept.setUpdateTime(LocalDateTime.now());
        //保存部门
        deptMapper.update(dept);
    }

}
