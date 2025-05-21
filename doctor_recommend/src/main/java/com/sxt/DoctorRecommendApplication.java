package com.sxt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@ServletComponentScan
@MapperScan("com.sxt.mapper")
@SpringBootApplication
public class DoctorRecommendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoctorRecommendApplication.class, args);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用明文密码编码器，不进行密码加密
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                // 直接返回原密码，不进行加密
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                // 直接比较原始密码和存储的密码
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }

}
