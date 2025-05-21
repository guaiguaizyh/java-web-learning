package com.sxt.service;

import com.sxt.pojo.User;
import java.util.Map;

public interface LoginService {

    Map<String, Object> login(String username, String password);

    boolean register(User user);

    boolean checkUsername(String username);
}
