package com.sxt.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// Department.java
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("departments")
public class Department {
    @TableId(type = IdType.AUTO)
    private Integer departmentId;
    private String departmentName;
    private String description;
    
    @TableField(exist = false)
    private Integer doctorCount;    // 科室医生数量
    @TableField(exist = false)
    private Integer reviewCount;    // 科室评论数量
    @TableField(exist = false)
    private Integer expertiseCount; // 科室关联的专长数量
    @TableField(exist = false)
    private Double averageRating;   // 科室平均评分
    
    // 用于验证的静态方法
    public static void validateDepartment(Department department) {
        if (department == null) {
            throw new IllegalArgumentException("科室信息不能为空");
        }
        if (department.getDepartmentName() == null || department.getDepartmentName().trim().isEmpty()) {
            throw new IllegalArgumentException("科室名称不能为空");
        }
    }
}   
