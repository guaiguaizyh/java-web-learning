package com.sxt.pojo;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
@Data
@TableName("symptoms")
public class SymptomDTO {
    private Integer symptomId;
    private String keyword;
    private Integer departmentId;
    
    // 科室名称，仅用于前端展示
    @TableField(exist = false)
    private String departmentName; // 科室名称，仅用于前端展示
    
    // 专长-症状关联度，仅用于前端展示
    @TableField(exist = false)
    private Float relevanceScore; // 与专长的关联度分数
}
