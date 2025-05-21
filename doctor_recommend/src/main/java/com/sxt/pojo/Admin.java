package com.sxt.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@TableName("t_admin")
public class Admin {
    @TableId(type = IdType.AUTO)
    private Long admin_id;
    @NotBlank(message = "用户名不能为空")
    @Length(min = 2, max = 20, message = "用户名长度必须在 2 到 20 个字符之间")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 20, message = "密码长度必须在 6 到 20 个字符之间")
    private String password;
}
