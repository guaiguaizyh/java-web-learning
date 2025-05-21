package com.sxt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sxt.mapper.PositionsMapper;
import com.sxt.pojo.Positions;
import com.sxt.pojo.dto.PositionDistributionDTO;
import com.sxt.service.PositionsService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PositionsServiceImpl extends ServiceImpl<PositionsMapper, Positions> implements PositionsService {
    
    @Override
    public List<Positions> getAllPositions() {
        return this.list();
    }
    
    @Override
    public Positions getPositionsById(Integer positionsId) {
        return this.getById(positionsId);
    }
    
    @Override
    public boolean addPositions(Positions positions) {
        return this.save(positions);
    }
    
    @Override
    public boolean updatePositions(Positions positions) {
        return this.updateById(positions);
    }
    
    @Override
    public boolean deletePositions(Integer positionsId) {
        return this.removeById(positionsId);
    }
    
    @Override
    public List<PositionDistributionDTO> getPositionsDistribution() {
        return this.baseMapper.getPositionsDistribution();
    }
} 