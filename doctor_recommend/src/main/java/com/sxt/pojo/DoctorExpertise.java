package com.sxt.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("doctor_expertise")
public class DoctorExpertise {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer doctorId;
    
    private Integer expertiseId;
    
    private String expertiseName;
    
    private Float proficiency;
    
    private Date createTime;
    
    private Date updateTime;
} 