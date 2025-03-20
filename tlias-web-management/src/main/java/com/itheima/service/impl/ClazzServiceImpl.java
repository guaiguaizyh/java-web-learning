package com.itheima.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.mapper.ClazzMapper;
import com.itheima.pojo.*;
import com.itheima.service.ClazzService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ClazzServiceImpl implements ClazzService {

    //注入Mapper接口依赖
    @Autowired
    private ClazzMapper clazzMapper;
//    @Autowired
//    private ClazzLogService clazzLogService;
    //private ClassService classService;

    //分页操作
    @Override
    public PageResult page(ClazzQueryParam clazzQueryParam) {
        //1.获取分页参数
        Integer page = clazzQueryParam.getPage();
        Integer pageSize = clazzQueryParam.getPageSize();
        //添加一个if 判断，如果page/pagesize为空，则默认为1
        if (page==null || pageSize==null ) {
            throw new IllegalArgumentException("分页参数不能为空") ;
        }
        //设置分页参数 --使用PageHelper插件
        PageHelper.startPage(page,pageSize);
        //执行查询 sql语句放在mapper中
        List<Clazz> clazzList = clazzMapper.clist(clazzQueryParam);
        //封装分页结果
        Page<Clazz> p = (Page<Clazz>) clazzList;
        return new PageResult(p.getTotal(),p.getResult());
    }

    //增加列表
    @SneakyThrows
    @Transactional//用于控制事务
    @Override
    public void insert(Clazz clazz) {
         // 处理默认值
            clazz.setCreateTime(LocalDateTime.now());
            clazz.setUpdateTime(LocalDateTime.now());
            // 保存员工基本信息数据
            clazzMapper.insert(clazz);
        // 插入成功后再查询完整信息（带 masterName）
        Clazz fullClazz = clazzMapper.getById(clazz.getId());
        log.info("新增后的完整信息: {}", fullClazz);
    }
    //删除列表
    @Transactional
    @Override
    public void deleteById(Integer id)
    {
        clazzMapper.deleteById(id);
    }
    //查询列表---数据回显
    @Override
    public Clazz getById(Integer id) {
       return clazzMapper.getById(id);
    }
    //修改列表
    @Override
    public void update(Clazz clazz) {
        clazzMapper.updateById(clazz);
        // 更新成功后再查询完整信息（带 masterName）
        Clazz fullClazz = clazzMapper.getById(clazz.getId());
        log.info("更新后的完整信息: {}", fullClazz);
    }
    //查询班级全部信息
    @Override
    public List<Clazz> findALL(){
        return clazzMapper.findALL();
    }
}
