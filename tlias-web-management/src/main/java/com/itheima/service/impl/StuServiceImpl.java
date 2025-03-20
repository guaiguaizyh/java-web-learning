package com.itheima.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.mapper.StuMapper;
import com.itheima.pojo.*;
import com.itheima.service.StuService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StuServiceImpl implements StuService {

    //注入Mapper接口依赖
    @Autowired
    private StuMapper stuMapper;
    //分页操作
    @Override
    public PageResult page(StuQueryParam stuQueryParam) {
        //1.获取分页参数
        Integer page = stuQueryParam.getPage();
        Integer pageSize = stuQueryParam.getPageSize();
        //添加一个if 判断，如果page/pagesize为空，则默认为1
        if (page==null || pageSize==null ) {
            throw new IllegalArgumentException("分页参数不能为空") ;
        }
        //设置分页参数 --使用PageHelper插件
        PageHelper.startPage(page,pageSize);
        //执行查询 sql语句放在mapper中
        List<Stu> stuList = stuMapper.stulist(stuQueryParam);
        //封装分页结果
        Page<Stu> p = (Page<Stu>) stuList;
        return new PageResult(p.getTotal(),p.getResult());
    }

    //批量删除
    @Transactional
    @Override
    public void deleteByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("ID 列表不能为空");
        }
        // 删除员工基本信息数据
        stuMapper.deleteByIds(ids);
    }

    //添加学员信息
    @SneakyThrows
    @Transactional(rollbackFor = {Exception.class})//用于控制事务
    //@Transactional
    @Override
    public void save(Stu stu) {
        stu.setCreateTime(LocalDateTime.now());
        stu.setUpdateTime(LocalDateTime.now());
        // 保存员工基本信息数据
        stuMapper.insert(stu);
    }

    //数据回显
    @Override
    public Stu getInfo(Integer id) {
        Stu stu = stuMapper.getById(id);
        return stu;
    }
    //修改列表
    @Override
    public void update(Stu stu)
    {
        stu.setUpdateTime(LocalDateTime.now());
        stuMapper.update(stu);
    }

    //违纪信息
    @Override
    public void handleViolation(Integer id, Integer score) {
        // 获取学员信息
        Stu stu = stuMapper.getById(id);
        if (stu != null) {
            // 增加违纪分数
            Short currentViolationScore = stu.getViolationScore();
            stu.setViolationScore((short) (currentViolationScore + score)); // 更新违纪分数

            // 增加违纪次数
            Short currentViolationCount = stu.getViolationCount();
            stu.setViolationCount((short) (currentViolationCount + 1)); // 违纪次数加1

            // 更新学员信息
            stuMapper.update(stu);
        } else {
            throw new RuntimeException("学员不存在");
        }
    }


}
