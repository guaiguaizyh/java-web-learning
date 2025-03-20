package com.itheima.service;

import com.github.pagehelper.PageInfo;
import com.itheima.pojo.LogQueryParam;
import com.itheima.pojo.OperateLog;
import com.itheima.pojo.PageResult;

public interface LogService {
    //分页显示日志数据
    //public List<OperateLog> findAll();
    PageResult page(LogQueryParam logQueryParam);
}
