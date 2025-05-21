package com.sxt.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("positions")
public class Positions {
    @TableId(type = IdType.AUTO)
    private Integer positionsId;
    
    private String positionsName;
    
    // 可以添加其他字段，如创建时间、更新时间等
} 