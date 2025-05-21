package com.sxt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sxt.pojo.User;
import com.sxt.pojo.vo.UserVO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    User selectByUsername(@Param("username") String username);
    int insertUser(User user);
    UserVO selectUserVoByUsername(@Param("username") String username);
    int updateUser(User user);
    
    /**
     * 更新用户名
     * @param userId 用户ID
     * @param username 新用户名
     * @return 影响的行数
     */
    @Update("UPDATE t_user SET username = #{username}, update_time = NOW() WHERE user_id = #{userId}")
    int updateUsername(@Param("userId") Long userId, @Param("username") String username);

    /**
     * 直接更新用户名
     */
    @Update("UPDATE t_user SET username = #{newUsername} WHERE user_id = #{userId}")
    int directUpdateUsername(@Param("userId") Long userId, @Param("newUsername") String newUsername);
}
