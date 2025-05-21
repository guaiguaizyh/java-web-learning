package com.sxt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sxt.pojo.Positions;
import com.sxt.pojo.dto.PositionDistributionDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PositionsMapper extends BaseMapper<Positions> {
    
    /**
     * 获取职称分布统计
     * @return 职称分布列表
     */
    List<PositionDistributionDTO> getPositionsDistribution();
} 