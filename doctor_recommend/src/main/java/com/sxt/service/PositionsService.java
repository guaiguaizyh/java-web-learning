package com.sxt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sxt.pojo.Positions;
import com.sxt.pojo.dto.PositionDistributionDTO;
import java.util.List;

public interface PositionsService extends IService<Positions> {
    List<Positions> getAllPositions();
    Positions getPositionsById(Integer positionsId);
    boolean addPositions(Positions positions);
    boolean updatePositions(Positions positions);
    boolean deletePositions(Integer positionsId);
    
    /**
     * 获取职称分布统计
     * @return 职称分布列表
     */
    List<PositionDistributionDTO> getPositionsDistribution();
} 