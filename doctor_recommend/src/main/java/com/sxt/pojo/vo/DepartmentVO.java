package com.sxt.pojo.vo;

import lombok.Data;

@Data
public class DepartmentVO {
    private Integer departmentId;
    private String departmentName;
    private String description;
    private Integer doctorCount;
    private Integer reviewCount;
    private Double averageRating;
} 