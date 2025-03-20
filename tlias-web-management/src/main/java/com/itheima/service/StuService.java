package com.itheima.service;

import com.itheima.pojo.PageResult;
import com.itheima.pojo.Stu;
import com.itheima.pojo.StuQueryParam;

import java.util.List;

public interface StuService {
    PageResult page(StuQueryParam stuQueryParam);//分页查询
    //批量删除
    void deleteByIds(List<Integer> idList);

    //添加
    void save(Stu stu);
    //修改数据回显
    Stu getInfo(Integer id);
    //修改信息
    void update(Stu stu);
    //违纪
    void handleViolation(Integer id, Integer score);
}
