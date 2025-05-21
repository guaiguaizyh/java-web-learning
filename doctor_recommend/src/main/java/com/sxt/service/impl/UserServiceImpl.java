package com.sxt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sxt.mapper.UserMapper;
import com.sxt.pojo.User;
import com.sxt.pojo.vo.UserVO;
import com.sxt.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Override
    public User findByUsername(String username) {
        log.debug("根据用户名查询用户: {}", username);
        return baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public boolean addUser(User user) {
        log.info("添加新用户: {}", user.getUsername());
        if (findByUsername(user.getUsername()) != null) {
            log.warn("用户名 '{}' 已存在，添加失败", user.getUsername());
            throw new IllegalArgumentException("用户名 '" + user.getUsername() + "' 已存在");
        }
        
        // 如果未提供密码，使用默认密码123456
        if (!StringUtils.hasText(user.getPassword())) {
            log.info("添加用户 '{}' 时未提供密码，使用默认密码123456", user.getUsername());
            user.setPassword("123456");
        }
        
        boolean success = save(user);
        if (success) {
            log.info("用户 '{}' 添加成功, ID: {}", user.getUsername(), user.getUserId());
        } else {
            log.error("用户 '{}' 添加失败", user.getUsername());
        }
        return success;
    }

    @Override
    public User getUserById(Long userId) {
        log.debug("根据ID获取用户信息: {}", userId);
        return baseMapper.selectById(userId);
    }

    @Override
    public IPage<User> getUserListPage(Page<User> page, String username, String phone, Integer minAge, Integer maxAge, String gender) {
        log.debug("获取用户分页列表，页码：{}，大小：{}，用户名：{}，手机号：{}，年龄范围：{}-{}，性别：{}",
                page.getCurrent(), page.getSize(), username, phone, minAge, maxAge, gender);
        Page<User> queryPage = new Page<>(page.getCurrent(), page.getSize());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(username)) {
            queryWrapper.like("username", username);
        }
        if (StringUtils.hasText(phone)) {
            queryWrapper.like("phone", phone);
        }
        if (minAge != null) {
            queryWrapper.ge("age", minAge);
        }
        if (maxAge != null) {
            queryWrapper.le("age", maxAge);
        }
        if (StringUtils.hasText(gender)) {
            queryWrapper.eq("gender", gender);
        }
        
        queryWrapper.orderByDesc("create_time");
        return baseMapper.selectPage(queryPage, queryWrapper);
    }

    @Override
    public boolean updateUserByAdmin(User incomingUser) {
        Long userId = incomingUser.getUserId();
        log.info("管理员更新用户信息，ID：{}，传入数据：{}", userId, incomingUser);
        if (userId == null) {
            log.error("更新用户失败：用户ID不能为空");
            throw new IllegalArgumentException("用户ID不能为空");
        }
        User existingUser = baseMapper.selectById(userId);
        if (existingUser == null) {
            log.warn("尝试更新的用户不存在，ID：{}", userId);
             return false;
        }

        boolean success = true;
        
        // 先处理用户名更新
        if (StringUtils.hasText(incomingUser.getUsername()) && !incomingUser.getUsername().equals(existingUser.getUsername())) {
            // 检查新用户名是否已被其他用户使用
            User userWithNewUsername = findByUsername(incomingUser.getUsername());
            if (userWithNewUsername != null && !userWithNewUsername.getUserId().equals(userId)) {
                throw new IllegalArgumentException("用户名已被使用");
            }
            log.info("正在更新用户名：{} -> {}", existingUser.getUsername(), incomingUser.getUsername());
            int rows = userMapper.directUpdateUsername(userId, incomingUser.getUsername());
            if (rows > 0) {
                log.info("用户名更新成功");
                existingUser.setUsername(incomingUser.getUsername());
            } else {
                log.error("用户名更新失败");
                success = false;
            }
        }

        // 更新其他信息
        if (success && (
            incomingUser.getPhone() != null ||
            incomingUser.getAge() != null ||
            incomingUser.getGender() != null ||
            incomingUser.getMedicalRecord() != null
        )) {
            // 设置要更新的字段
            User updateUser = new User();
            updateUser.setUserId(userId);
            updateUser.setUsername(existingUser.getUsername());  // 使用最新的用户名
            updateUser.setPassword(existingUser.getPassword());  // 保持原密码不变
            
            if (incomingUser.getPhone() != null) {
                if (!incomingUser.getPhone().matches("^1[3-9]\\d{9}$")) {
                    throw new IllegalArgumentException("手机号格式不正确");
                }
                updateUser.setPhone(incomingUser.getPhone());
            } else {
                updateUser.setPhone(existingUser.getPhone());
            }
            
            if (incomingUser.getAge() != null) {
                if (incomingUser.getAge() < 0 || incomingUser.getAge() > 150) {
                    throw new IllegalArgumentException("年龄不合法");
                }
                updateUser.setAge(incomingUser.getAge());
            } else {
                updateUser.setAge(existingUser.getAge());
            }
            
            if (incomingUser.getGender() != null) {
                if (!incomingUser.getGender().matches("^[男女]$")) {
                    throw new IllegalArgumentException("性别只能是'男'或'女'");
                }
                updateUser.setGender(incomingUser.getGender());
            } else {
                updateUser.setGender(existingUser.getGender());
            }
            
            updateUser.setMedicalRecord(incomingUser.getMedicalRecord() != null ? 
                incomingUser.getMedicalRecord() : existingUser.getMedicalRecord());

            // 执行其他信息的更新
            try {
                success = userMapper.updateUser(updateUser) > 0;
                if (success) {
                    log.info("用户其他信息更新成功");
                } else {
                    log.error("用户其他信息更新失败");
                }
            } catch (Exception e) {
                log.error("更新用户其他信息时发生错误：{}", e.getMessage());
                success = false;
            }
        }

        if (success) {
            log.info("管理员更新用户 {} 所有信息成功。", userId);
        } else {
            log.error("管理员更新用户 {} 信息过程中发生错误。", userId);
        }
        return success;
    }

    @Override
    public boolean updateUserSelf(User incomingUser) {
         Long currentUserId = getCurrentUserId();
         if (currentUserId == null) {
             log.error("无法获取当前登录用户ID，更新失败");
             throw new IllegalStateException("无法确定当前用户身份");
         }
         if (incomingUser.getUserId() != null && !currentUserId.equals(incomingUser.getUserId())) {
             log.warn("用户 {} 尝试更新用户 {} 的信息，操作被阻止。", currentUserId, incomingUser.getUserId());
             throw new SecurityException("无权修改其他用户信息");
         }
         incomingUser.setUserId(currentUserId);

        log.info("用户 {} 更新自己的信息: {}", currentUserId, incomingUser);
        User existingUser = baseMapper.selectById(currentUserId);
        if (existingUser == null) {
            log.error("当前登录用户 {} 在数据库中不存在！", currentUserId);
             throw new IllegalStateException("当前用户数据异常");
        }

        boolean changed = false;

        if (incomingUser.getPhone() != null && !incomingUser.getPhone().equals(existingUser.getPhone())) {
             if (!incomingUser.getPhone().matches("^1[3-9]\\d{9}$")) {
                 throw new IllegalArgumentException("手机号格式不正确");
             }
            log.debug("用户ID: {}, 手机号由 '{}' 更新为 '{}'", currentUserId, existingUser.getPhone(), incomingUser.getPhone());
            existingUser.setPhone(incomingUser.getPhone());
            changed = true;
        }
        if (incomingUser.getAge() != null && !incomingUser.getAge().equals(existingUser.getAge())) {
             if (incomingUser.getAge() < 0 || incomingUser.getAge() > 150) {
                 throw new IllegalArgumentException("年龄不合法");
             }
            log.debug("用户ID: {}, 年龄由 '{}' 更新为 '{}'", currentUserId, existingUser.getAge(), incomingUser.getAge());
            existingUser.setAge(incomingUser.getAge());
            changed = true;
        }
        if (incomingUser.getGender() != null && !incomingUser.getGender().equals(existingUser.getGender())) {
            if (!incomingUser.getGender().matches("^[男女]$")) {
                throw new IllegalArgumentException("性别只能是'男'或'女'");
            }
            log.debug("用户ID: {}, 性别由 '{}' 更新为 '{}'", currentUserId, existingUser.getGender(), incomingUser.getGender());
            existingUser.setGender(incomingUser.getGender());
            changed = true;
        }
        if (incomingUser.getMedicalRecord() != null && !incomingUser.getMedicalRecord().equals(existingUser.getMedicalRecord())) {
            log.debug("用户ID: {}, 病历信息更新", currentUserId);
            existingUser.setMedicalRecord(incomingUser.getMedicalRecord());
            changed = true;
        }

        if (StringUtils.hasText(incomingUser.getPassword())) {
            log.warn("用户 {} 尝试通过更新个人信息接口修改密码，操作被阻止。", currentUserId);
        }
        if (StringUtils.hasText(incomingUser.getUsername()) && !incomingUser.getUsername().equals(existingUser.getUsername())) {
             log.warn("用户 {} 尝试通过更新个人信息接口修改用户名，操作被阻止。", currentUserId);
        }

        if (changed) {
            log.info("用户 {} 更新个人信息成功。", currentUserId);
            return userMapper.updateUser(existingUser) > 0;
        } else {
            log.info("用户 {} 的信息未发生变化，无需更新。", currentUserId);
            return true;
        }
    }

    @Override
    public boolean changePassword(String oldPassword, String newPassword, Long userId) {
        log.info("用户 {} 尝试修改密码", userId);
        if (userId == null) {
            log.error("修改密码失败：用户ID不能为空");
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (!StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
             log.warn("用户 {} 修改密码失败：旧密码或新密码为空", userId);
             throw new IllegalArgumentException("旧密码和新密码都不能为空");
        }
        if (oldPassword.equals(newPassword)){
             log.warn("用户 {} 修改密码失败：新旧密码不能相同", userId);
             throw new IllegalArgumentException("新旧密码不能相同");
        }
        User user = baseMapper.selectById(userId);
        if (user == null) {
            log.error("修改密码失败：用户 {} 不存在", userId);
            throw new IllegalArgumentException("用户不存在");
        }
        if (!oldPassword.equals(user.getPassword())) {
            log.warn("用户 {} 修改密码失败：旧密码不正确", userId);
            throw new IllegalArgumentException("旧密码不正确");
        }
        user.setPassword(newPassword);
        boolean success = userMapper.updateUser(user) > 0;
        if (success) {
            log.info("用户 {} 修改密码成功", userId);
        } else {
            log.error("用户 {} 修改密码时数据库更新失败", userId);
        }
        return success;
    }

    @Override
    public User getUserInfo(Long userId) {
        log.debug("获取用户 {} 的详细信息", userId);
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return baseMapper.selectById(userId);
    }

    @Override
    public boolean deleteUserById(Long userId) {
        log.info("删除用户：{}", userId);
        if (userId == null) {
            log.error("删除用户失败：用户ID不能为空");
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return removeById(userId);
    }

    @Override
    public UserVO getUserInfoByUsername(String username) {
        // 直接调用返回 UserVO 的新 Mapper 方法
        return userMapper.selectUserVoByUsername(username);
    }

    @Override
    public User getUserByUsername(String username) {
        // 调用返回 User POJO 的方法
        return userMapper.selectByUsername(username);
    }

    @Override
    public boolean registerUser(User user) {
        log.info("注册新用户: {}", user.getUsername());
        // 验证用户名是否已存在
        if (findByUsername(user.getUsername()) != null) {
            log.warn("用户名 '{}' 已存在，注册失败", user.getUsername());
            throw new IllegalArgumentException("用户名已存在");
        }
        // 验证必填字段
        if (!StringUtils.hasText(user.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        // 如果未提供密码，使用默认密码123456
        if (!StringUtils.hasText(user.getPassword())) {
            log.info("用户 '{}' 注册时未提供密码，使用默认密码", user.getUsername());
            user.setPassword("123456");
        }
        // 验证手机号格式（如果提供）
        if (StringUtils.hasText(user.getPhone()) && !user.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        // 验证年龄（如果提供）
        if (user.getAge() != null && (user.getAge() < 0 || user.getAge() > 150)) {
            throw new IllegalArgumentException("年龄不合法");
        }

        // 保存用户
        boolean success = save(user);
        if (success) {
            log.info("用户 '{}' 注册成功, ID: {}", user.getUsername(), user.getUserId());
        } else {
            log.error("用户 '{}' 注册失败", user.getUsername());
        }
        return success;
    }

    @Override
    public IPage<UserVO> getUserVOListPage(Page<User> page, String username, String phone, Integer minAge, Integer maxAge, String gender) {
        log.debug("获取用户VO分页列表，页码：{}，大小：{}，用户名：{}，手机号：{}，年龄范围：{}-{}，性别：{}",
                page.getCurrent(), page.getSize(), username, phone, minAge, maxAge, gender);
                
        // 先获取User分页数据
        IPage<User> userPage = getUserListPage(page, username, phone, minAge, maxAge, gender);
        // 创建新的Page对象用于UserVO
        Page<UserVO> voPage = new Page<>(page.getCurrent(), page.getSize());
        voPage.setTotal(userPage.getTotal());
        // 转换记录
        voPage.setRecords(userPage.getRecords().stream()
                .map(this::convertToUserVO)
                .toList());
        
        return voPage;
    }
    @Override
    public boolean updateUserProfile(UserVO userVO) {
        log.info("更新用户个人信息，用户ID：{}", userVO.getUserId());
        
        if (userVO.getUserId() == null) {
            log.error("更新用户个人信息失败：用户ID不能为空");
            throw new IllegalArgumentException("用户ID不能为空");
        }
        // 获取现有用户信息
        User existingUser = baseMapper.selectById(userVO.getUserId());
        if (existingUser == null) {
            log.warn("更新失败：用户不存在，ID：{}", userVO.getUserId());
            return false;
        }
        // 更新允许修改的字段
        boolean changed = false;
        if (userVO.getPhone() != null && !userVO.getPhone().equals(existingUser.getPhone())) {
            if (!userVO.getPhone().matches("^1[3-9]\\d{9}$")) {
                throw new IllegalArgumentException("手机号格式不正确");
            }
            existingUser.setPhone(userVO.getPhone());
            changed = true;
        }
        if (userVO.getAge() != null && !userVO.getAge().equals(existingUser.getAge())) {
            if (userVO.getAge() < 0 || userVO.getAge() > 150) {
                throw new IllegalArgumentException("年龄不合法");
            }
            existingUser.setAge(userVO.getAge());
            changed = true;
        }
        if (userVO.getGender() != null && !userVO.getGender().equals(existingUser.getGender())) {
            existingUser.setGender(userVO.getGender());
            changed = true;
        }
        if (userVO.getMedicalRecord() != null && !userVO.getMedicalRecord().equals(existingUser.getMedicalRecord())) {
            existingUser.setMedicalRecord(userVO.getMedicalRecord());
            changed = true;
        }
        if (changed) {
            // 手动设置更新时间为当前时间，确保即使MyBatis-Plus的自动填充不起作用，更新时间也会更新
            existingUser.setUpdateTime(new Date());
            log.info("用户 {} 的个人信息已更新，更新时间设置为：{}", userVO.getUserId(), existingUser.getUpdateTime());
            return userMapper.updateUser(existingUser) > 0;
        } else {
            log.info("用户 {} 的信息未发生变化，无需更新", userVO.getUserId());
            return true;
        }
    }

    @Override
    public UserVO convertToUserVO(User user) {
        if (user == null) {
            return null;
        }

        UserVO vo = new UserVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setAge(user.getAge());
        vo.setGender(user.getGender());
        vo.setPhone(user.getPhone());
        vo.setMedicalRecord(user.getMedicalRecord());
        // 暂时不设置头像URL
        return vo;
    }

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = findByUsername(username);
        return user != null ? user.getUserId() : null;
    }
} 