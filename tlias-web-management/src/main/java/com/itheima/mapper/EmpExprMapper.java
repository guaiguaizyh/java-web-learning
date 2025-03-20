package com.itheima.mapper;

import com.itheima.pojo.EmpExpr;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EmpExprMapper {

    //批量插入员工工作经历信息
    public void insertBatch(List<EmpExpr> empExprList);

    //删除员工的工作信息
    void deleteByEmpIds(List<Integer> ids);
}
