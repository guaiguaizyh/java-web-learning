package com.sxt.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("reviews") // 映射数据库的 reviews 表
public class DoctorReview {

    @TableId(value = "review_id", type = IdType.AUTO)
    private Long reviewId;

    @TableField("doctor_id")
    private Integer doctorId; // 假设 Doctor ID 是 Integer

    @TableField("user_id")
    private Long userId; // 假设 User ID 是 Long

    @TableField("rating")
    private Integer rating; // 评分 (1-5)

    @TableField("comment")
    private String comment;

    @TableField("created_at") // 数据库是 created_at
    private Date createdAt; // createTime 或 reviewTime 可能更符合 Java 命名习惯，但需与数据库一致

    @TableField("is_anonymous")
    private Boolean isAnonymous; // 0 或 1 映射为 false 或 true

    // --- 以下为可选的关联字段，用于方便前端显示，但不在 reviews 表中 ---

    @TableField(exist = false)
    private String username; // 评论者用户名 (如果非匿名)

    @TableField(exist = false)
    private String userAvatar; // 评论者头像 (如果需要显示)

    @TableField(exist = false)
    private String doctorName; // 医生姓名

    @TableField(exist = false)
    private String departmentName; // 科室名称
} 