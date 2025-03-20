package com.itheima.pojo;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler
    public Result ex(Exception e){
        e.printStackTrace();//打印异常信息
        //捕获到异常之后，响应一个标准的Result
        return Result.error("操作失败");
    }
}
