package com.sxt.service.impl;

import com.sxt.mapper.AdminMapper;
import com.sxt.mapper.UserMapper;
import com.sxt.pojo.Admin;
import com.sxt.pojo.User;
import com.sxt.service.LoginService;
import com.sxt.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();
        UserDetails userDetails = null;
        String role = null;
        Object userObject = null;

        try {
            userDetails = userDetailsService.loadUserByUsername(username);
        } catch (Exception e) {
            return null; // 用户不存在
        }

        // Compare plain text passwords
        if (userDetails != null && userDetails.getPassword() != null && userDetails.getPassword().equals(password)) {
            // Password matches, generate JWT
            String jwt = jwtUtils.generateToken(userDetails);
            result.put("token", jwt);
            // Determine role and fetch user/admin object
            if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                role = "admin";
                userObject = adminMapper.selectByUsername2(username);
                // Nullify password if needed for admin object as well
                if (userObject instanceof Admin) {
                    ((Admin) userObject).setPassword(null); // Assuming Admin has setPassword
                }
            } else {
                role = "user";
                userObject = userMapper.selectByUsername(username);
                if (userObject instanceof User) {
                    ((User) userObject).setPassword(null);
                }
            }
            result.put("role", role);
            result.put("user", userObject);

            return result;
        } else {
            // Password does not match
            return null;
        }
    }

    @Override
    public boolean register(User user) {
        if (checkUsername(user.getUsername())) {
            return false; // Username exists
        }
        // Save plain text password directly (NO ENCODING)
        // user.setPassword(user.getPassword()); // Already set
        return userMapper.insertUser(user) > 0;
    }

    @Override
    public boolean checkUsername(String username) {
        return userMapper.selectByUsername(username) != null || 
               adminMapper.selectByUsername2(username) != null;
    }
}
