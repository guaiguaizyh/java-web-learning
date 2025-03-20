package com.itheima.pojo;

import lombok.Data;
import java.io.Serializable;

@Data
public class Result {

    private Integer code;//根据接口文档，1表示成功，0表示失败
    private String msg;//返回结果信息
    private Object data;//返回数据

    public static Result success() {
        //请求成功之后调用，封装数据
        Result result = new Result();
        result.code = 1;
        result.msg = "success";
        return result;
    }

    public static Result success(Object object) {
        Result result = new Result();
        result.data = object;
        result.code = 1;
        result.msg = "success";
        return result;
    }

    public static Result error(String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = 0;
        return result;
    }

}