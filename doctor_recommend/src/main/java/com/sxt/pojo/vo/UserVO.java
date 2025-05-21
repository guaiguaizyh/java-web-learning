package com.sxt.pojo.vo; // 或者你的 VO 包路径

import lombok.Data;

/**
 * 用于API返回的用户信息视图对象
 */
@Data
public class UserVO {

    private Long userId; // 用户ID
    private String username; // 用户名
    // 注意：通常不包含 password 字段
    private Integer age; // 年龄
    private String gender; // 性别 (将包含 "男", "女" 等标签)
    private String phone; // 手机号
    private String medicalRecord; // 病历信息
    private String avatarUrl;
}
