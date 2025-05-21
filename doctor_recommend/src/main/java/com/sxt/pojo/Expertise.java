package com.sxt.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("expertises")
public class Expertise {
    @TableId(type = IdType.AUTO)
    private Integer expertiseId;
    
    private String expertiseName;
    
    private Integer departmentId;

    //封装部门名称
    @TableField(exist = false)
    private String departmentName;
    
    @TableField(exist = false)
    private Float proficiency;  // 医生对该专长的熟练度

    @TableField(exist = false)
    private Float avgProficiency; // 所有医生的平均熟练度

    @TableField(exist = false)
    private Float relevanceScore; // 与症状的相关性分数
    
    @TableField(exist = false)
    private Integer symptomCount; // 关联的症状数量

    @TableField(exist = false)
    private Integer doctorCount; // 关联的医生数量
}