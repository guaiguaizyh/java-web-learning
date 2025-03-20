package com.itheima.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.itheima.mapper.LogMapper;
import com.itheima.pojo.*;
import com.itheima.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private LogMapper logMapper;
//    @Override
//    public PageResult page(LogQueryParam logQueryParam) {
//        //1.获取分页参数
//        Integer page = logQueryParam.getPage();
//        Integer pageSize = logQueryParam.getPageSize();
//        //添加一个if 判断，如果page/pagesize为空，则默认为1
//        if (page==null || pageSize==null ) {
//            throw new IllegalArgumentException("分页参数不能为空") ;
//        }
//        //设置分页参数 --使用PageHelper插件
//        PageHelper.startPage(page,pageSize);
//        //执行查询 sql语句放在mapper中
//        List<OperateLog> logList = OperateLogMapper.list();
//        //封装分页结果
//        Page<OperateLog> p = (Page<OperateLog>) logList;
//        return new PageResult(p.getTotal(),p.getResult());
//    }
//public List<OperateLog> findAll()
//{
//    return logMapper.findAll();
//}
@Override
public PageResult page(LogQueryParam logQueryParam) {
    // 检查并设置默认值
    if (logQueryParam.getPage() == null || logQueryParam.getPageSize() == null) {
        throw new IllegalArgumentException("分页参数不能为空");
    }
    // 设置PageHelper分页参数
    PageHelper.startPage(logQueryParam.getPage(), logQueryParam.getPageSize());
    // 执行查询
    List<OperateLog> List = logMapper.findAll();
    // 封装分页结果
    Page<OperateLog> p = (Page<OperateLog>) List;
    return new PageResult(p.getTotal(), p.getResult());
}
//@Override
//public PageInfo<OperateLog> findByPage(Integer page, Integer pageSize) {
//    // 1. 开启分页
//    PageHelper.startPage(page, pageSize);
//    // 2. 执行查询（原查询所有的方法）
//    List<OperateLog> list = logMapper.findAll();
//    // 3. 封装分页结果（包含总条数、总页数、当前页数据等）
//    return new PageInfo<>(list);
//}

}
