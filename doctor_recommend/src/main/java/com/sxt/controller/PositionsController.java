package com.sxt.controller;

import com.sxt.pojo.Positions;
import com.sxt.pojo.Result;
import com.sxt.pojo.dto.PositionDistributionDTO;
import com.sxt.service.PositionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/positions")
public class PositionsController {
    
    @Autowired
    private PositionsService positionsService;
    
    @GetMapping
    public Result<List<Positions>> getAllPositions() {
        try {
            log.info("获取所有职称信息");
            List<Positions> positionsList = positionsService.getAllPositions();
            return Result.success(positionsList);
        } catch (Exception e) {
            log.error("获取职称列表失败", e);
            return Result.error("获取职称列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取职称分布统计
     * @return 职称分布统计列表
     */
    @GetMapping("/distribution")
    public Result<List<PositionDistributionDTO>> getPositionsDistribution() {
        try {
            log.info("获取职称分布统计");
            List<PositionDistributionDTO> distribution = positionsService.getPositionsDistribution();
            return Result.success(distribution);
        } catch (Exception e) {
            log.error("获取职称分布统计失败", e);
            return Result.error("获取职称分布统计失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/{positionsId}")
    public Result<Positions> getPositionsById(@PathVariable Integer positionsId) {
        try {
            log.info("获取职称详情, positionsId: {}", positionsId);
            Positions positions = positionsService.getPositionsById(positionsId);
            if (positions == null) {
                return Result.error("未找到指定职称");
            }
            return Result.success(positions);
        } catch (Exception e) {
            log.error("获取职称详情失败", e);
            return Result.error("获取职称详情失败：" + e.getMessage());
        }
    }
    
    @PostMapping
    public Result<Boolean> addPositions(@RequestBody Positions positions) {
        try {
            log.info("添加职称, positions: {}", positions);
            if (positions.getPositionsName() == null || positions.getPositionsName().trim().isEmpty()) {
                return Result.error("职称名称不能为空");
            }
            return Result.success(positionsService.addPositions(positions));
        } catch (Exception e) {
            log.error("添加职称失败", e);
            return Result.error("添加职称失败：" + e.getMessage());
        }
    }
    
    @PutMapping("/{positionsId}")
    public Result<Boolean> updatePositions(@PathVariable Integer positionsId, @RequestBody Positions positions) {
        try {
            log.info("更新职称, positionsId: {}, positions: {}", positionsId, positions);
            positions.setPositionsId(positionsId);
            return Result.success(positionsService.updatePositions(positions));
        } catch (Exception e) {
            log.error("更新职称失败", e);
            return Result.error("更新职称失败：" + e.getMessage());
        }
    }
    
    @DeleteMapping("/{positionsId}")
    public Result<Boolean> deletePositions(@PathVariable Integer positionsId) {
        try {
            log.info("删除职称, positionsId: {}", positionsId);
            return Result.success(positionsService.deletePositions(positionsId));
        } catch (Exception e) {
            log.error("删除职称失败", e);
            return Result.error("删除职称失败：" + e.getMessage());
        }
    }
} 