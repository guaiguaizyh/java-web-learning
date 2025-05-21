package com.sxt.pojo.dto;

import lombok.Data;

/**
 * 职称分布数据传输对象
 */
@Data
public class PositionDistributionDTO {
    /**
     * 职称ID
     */
    private Integer positionsId;
    
    /**
     * 职称名称
     */
    private String positionsName;
    
    /**
     * 医生数量
     */
    private Integer doctorCount;
} 