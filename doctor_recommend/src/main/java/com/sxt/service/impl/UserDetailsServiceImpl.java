package com.sxt.service.impl;

import com.sxt.mapper.AdminMapper;
import com.sxt.mapper.UserMapper;
import com.sxt.pojo.Admin;
import com.sxt.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 尝试从用户表加载
        User user = userMapper.selectByUsername(username);
        if (user != null) {
            // 创建权限列表
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // 分配 'ROLE_USER' 角色

            // 返回 Spring Security 的 UserDetails 实现
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(), // 密码应该是数据库中存储的哈希值
                    authorities
            );
        }
        // 2. 如果用户表没有，尝试从管理员表加载
        Admin admin = adminMapper.selectByUsername2(username);
        if (admin != null) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN")); // 分配 'ROLE_ADMIN' 角色
            authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // 管理员通常也具有普通用户权限

            return new org.springframework.security.core.userdetails.User(
                    admin.getUsername(),
                    admin.getPassword(),
                    authorities
            );
        }
        // 3. 如果两个表都没有找到用户
        throw new UsernameNotFoundException("用户 '" + username + "' 未找到");
    }
} 