package com.itheima.interceptor;

import com.itheima.utils.CurrentHolder;
import com.itheima.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Override
    //引入接口HandlerIntercepor之后---Ctrl+O :重写preHandle方法
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        //1. 获取请求url。
//        String url = request.getRequestURL().toString();
//        //2. 判断请求url中是否包含login，如果包含，说明是登录操作，放行。
//        if(url.contains("/login")){ //登录请求
//            log.info("登录请求 , 直接放行");
//            //chain.doFilter(request, response);
//            return true;
//        }

        //3. 获取请求头中的令牌（token）。
        String jwt = request.getHeader("token");
        //4. 判断令牌是否存在，如果不存在，返回错误结果（未登录）。
        if(!StringUtils.hasLength(jwt)){ //jwt为空
            log.info("获取到jwt令牌为空, 返回错误结果");
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return true;
        }

        //5. 解析token，如果解析失败，返回错误结果（未登录）。
        try {
            Claims claims = JwtUtils.parseJWT(jwt);
            Integer empId = Integer.valueOf(claims.get("id").toString());
            CurrentHolder.setCurrentId(empId); // 🚨 存储当前用户 ID
            log.info("当前员工 ID：{}，放行", empId);
            JwtUtils.parseJWT(jwt);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("解析令牌失败, 返回错误结果");
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return true;
        }

        //6. 放行。
        log.info("令牌合法, 放行");
        return true;
        //chain.doFilter(request , response);
    }
}
