package com.itheima;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {
    @Test
    public void testGenJwt(){
        Map<String, Object> dataMap = new HashMap<>(); //创建一个Map集合，用来存储自定义的JWT信息
        dataMap.put("id",1);//存储用户id值为1
        dataMap.put("username","admin");//存储用户名
        //使用jwts.builder()创建jwt构造器
        String jwt = Jwts.builder()
                .signWith(SignatureAlgorithm.HS256,"aXRoZWltYQ==")//使用HS26签名算法，并指定密钥为：itheime转为base64
                .addClaims(dataMap)//设置自定义的JWT信息
                .setExpiration(new Date(System.currentTimeMillis()+12 * 3600 * 1000))
                //设置过期时间为当前加12小时:(3600ms*1000=3600s)*12=12h
                .compact();//生成并压缩JWT字符串
        System.out.println(jwt);
    }

    //校验JWT令牌(解析生成的令牌)
    @Test
    public void testParseJwt(){
        Claims claims= Jwts.parser()
                .setSigningKey("aXRoZWltYQ==")//设置密钥
                .parseClaimsJws("eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwidXNlcm5hbWUiOiJhZG1pbiIsImV4cCI6MTc0MTgxMTQ2N30.yFownXoJq-ptTvD9dPRhg3Brdw0f9ySeyV2m8oHET-w")
                .getBody();//解析JWT字符串，获取Claims对象
        System.out.println(claims);
    }
}
