package com.itheima.aop;

import com.itheima.mapper.LogMapper;
import com.itheima.pojo.OperateLog;
import com.itheima.utils.CurrentHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private LogMapper logMapper;
    // 环绕通知
    @Around("@annotation(com.itheima.anno.LogOperation)")//表示只有用此包下的logoperation注解的方式还会被拦截
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        // 执行方法
        Object result = joinPoint.proceed();
        // 当前时间
        long endTime = System.currentTimeMillis();
        // 耗时
        long costTime = endTime - startTime;
        // 构建日志对象
        OperateLog operateLog = new OperateLog();
        operateLog.setOperateEmpId(getCurrentUserId()); //操作人的id 需要实现 getCurrentUserId 方法
        operateLog.setOperateTime(LocalDateTime.now());//当前操作时间
        operateLog.setClassName(joinPoint.getTarget().getClass().getName());//类名
        operateLog.setMethodName(joinPoint.getSignature().getName());//方法名
        operateLog.setMethodParams(Arrays.toString(joinPoint.getArgs()));//参数
        operateLog.setReturnValue(result.toString());//返回值
        operateLog.setCostTime(costTime);//耗时
        operateLog.setOperateEmpName(log.getName());//操作人姓名
        //保存日志
        log.info("操作日志：{}",operateLog);
        // 插入日志
        logMapper.insert(operateLog);
        return result;
    }
    // 示例方法，获取当前用户ID
    private int getCurrentUserId() {
        // 这里应该根据实际情况从认证信息中获取当前登录用户的ID
        return CurrentHolder.getCurrentId(); // 返回当前登录用户的ID
    }
}