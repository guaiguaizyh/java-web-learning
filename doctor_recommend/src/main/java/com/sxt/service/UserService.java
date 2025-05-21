package com.sxt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sxt.pojo.User;
import com.sxt.pojo.vo.UserVO;

public interface UserService extends IService<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    User findByUsername(String username);

    /**
     * 管理员添加用户
     *
     * @param user 包含新用户信息的实体 (密码应为明文，Service层负责加密)
     * @return 操作结果
     */
    boolean addUser(User user);

    /**
     * 管理员根据ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户实体 (包含所有字段，包括密码哈希)
     */
    User getUserById(Long userId);

    /**
     * 管理员分页查询用户列表
     *
     * @param page 分页参数
     * @param username 用户名 (可选过滤条件)
     * @param phone 手机号 (可选过滤条件)
     * @param minAge 最小年龄 (可选过滤条件)
     * @param maxAge 最大年龄 (可选过滤条件)
     * @param gender 性别 (可选过滤条件)
     * @return 用户实体分页结果 (包含所有字段，包括密码哈希)
     */
    IPage<User> getUserListPage(Page<User> page, String username, String phone, Integer minAge, Integer maxAge, String gender);

    /**
     * 管理员分页查询用户列表（返回VO对象）
     *
     * @param page 分页参数
     * @param username 用户名 (可选过滤条件)
     * @param phone 手机号 (可选过滤条件)
     * @param minAge 最小年龄 (可选过滤条件)
     * @param maxAge 最大年龄 (可选过滤条件)
     * @param gender 性别 (可选过滤条件)
     * @return UserVO对象分页结果 (不含敏感信息)
     */
    IPage<UserVO> getUserVOListPage(Page<User> page, String username, String phone, Integer minAge, Integer maxAge, String gender);

    /**
     * 管理员更新用户信息
     *
     * @param user 包含更新信息的实体 (Service层应控制只更新允许的字段)
     * @return 操作结果
     */
    boolean updateUserByAdmin(User user);

    /**
     * 用户自己更新个人信息
     *
     * @param user 包含更新信息的实体 (Service层应控制只更新允许的字段)
     * @return 操作结果
     */
    boolean updateUserSelf(User user);

    /**
     * 用户更新个人信息（使用VO对象）
     *
     * @param userVO 包含更新信息的VO对象
     * @return 操作结果
     */
    boolean updateUserProfile(UserVO userVO);

    /**
     * 修改密码
     *
     * @param oldPassword 旧密码 (明文)
     * @param newPassword 新密码 (明文)
     * @param userId 用户ID
     * @return 操作结果
     */
    boolean changePassword(String oldPassword, String newPassword, Long userId);

    /**
     * 获取指定用户的详细信息
     *
     * @param userId 用户ID
     * @return 用户实体 (包含所有字段，包括密码哈希)
     */
    User getUserInfo(Long userId);

    /**
     * 根据用户ID删除用户
     * @param userId 用户ID
     * @return boolean
     */
    boolean deleteUserById(Long userId);

    /**
     * 根据用户名获取用户信息 (用于API展示)
     * @param username 用户名
     * @return UserVO 包含性别标签等信息，或 null 如果用户不存在
     */
    UserVO getUserInfoByUsername(String username);

    /**
     * 根据用户名获取用户实体 (用于需要原始数据的内部逻辑)
     * @param username 用户名
     * @return User 包含原始编码信息
     */
    User getUserByUsername(String username);

    /**
     * 用户注册
     * @param user 包含用户信息的 User 对象 (gender 应为编码)
     * @return 是否注册成功
     */
    boolean registerUser(User user);

    /**
     * 将 User 实体转换为 UserVO
     * @param user User实体
     * @return UserVO对象
     */
    UserVO convertToUserVO(User user);
} 