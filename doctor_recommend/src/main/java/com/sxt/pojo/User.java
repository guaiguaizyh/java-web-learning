package com.sxt.pojo;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.Date;

@Data
@TableName("t_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long userId;
    @NotBlank(message = "用户名不能为空")
    @Length(min = 2, max = 20, message = "用户名长度必须在 2 到 20 个字符之间")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 20, message = "密码长度必须在 6 到 20 个字符之间")
    private String password;

    @NotNull(message = "年龄不能为空")
    @Min(value = 0, message = "年龄不能小于 0")
    @Max(value = 100, message = "年龄不能大于 100")
    private Integer age;

    @NotNull(message = "请选择性别")
    @Pattern(regexp = "^[男女]$", message = "性别只能是'男'或'女'")
    private String gender;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String medicalRecord;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}