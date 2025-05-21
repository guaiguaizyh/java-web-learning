package com.sxt.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("doctors")
public class Doctor {
    @TableId(type = IdType.AUTO)
    private Integer doctorId;
    
    private String name;
    private String gender;
    private Integer age;
    private Integer positionsId;  // 职称ID，关联positions表
    private Integer departmentId;
    private String avatarUrl;
    private Float averageRating;
    private Integer ratingCount;
    private Integer workYears;  // 工作时长/年限

    // 关联字段（通过 MyBatis Plus 填充）
    @TableField(exist = false)
    private String departmentName;  // 科室名称

    @TableField(exist = false)
    private String expertiseList;  // 专长列表（字符串形式）
    
    @TableField(exist = false)
    private String positionsName;  // 职称名称（显示用）
}



