package com.itheima.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor//有了全参构造，就不需要空参构造了
@AllArgsConstructor//构造器，建立全参构造
//分页结果封装类
public class PageResult<T> {
    private Long total;
    //total与接口文档保持一致总记录数
    private List<T> rows;
    //指定一个范型：T
    // rows与接口文档保持一致当前页数据列表
}
