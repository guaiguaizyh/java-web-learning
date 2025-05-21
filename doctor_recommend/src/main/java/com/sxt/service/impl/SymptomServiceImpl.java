package com.sxt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sxt.mapper.SymptomMapper;
import com.sxt.pojo.SymptomDTO;
import com.sxt.service.SymptomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SymptomServiceImpl extends ServiceImpl<SymptomMapper, SymptomDTO> implements SymptomService {

    @Override
    public IPage<SymptomDTO> getSymptomList(int page, int size, String keyword, Integer departmentId) {
        log.info("Fetching symptoms page {} with size {}, keyword: {}, departmentId: {}", 
                page, size, keyword, departmentId);
        Page<SymptomDTO> pageInfo = new Page<>(page, size);
        return baseMapper.selectSymptomPage(pageInfo, keyword, departmentId);
    }

    @Override
    public SymptomDTO getSymptomById(Integer id) {
        log.info("Fetching symptom with id: {}", id);
        return getById(id);
    }

    @Override
    public boolean addSymptom(SymptomDTO symptom) {
        log.info("Adding new symptom: {}", symptom);
        return save(symptom);
    }

    @Override
    public boolean updateSymptom(SymptomDTO symptom) {
        log.info("Updating symptom: {}", symptom);
        
        try {
            // 直接执行更新，不做额外检查
            int rows = baseMapper.updateById(symptom);
            return rows > 0;
        } catch (Exception e) {
            log.error("Error updating symptom: ", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean deleteSymptom(Integer id) {
        log.info("Deleting symptom with id: {}", id);
        return removeById(id);
    }

    @Override
    public List<SymptomDTO> getSymptomsByExpertiseId(Integer expertiseId) {
        log.info("Fetching symptoms for expertise id: {}", expertiseId);
        if (expertiseId == null) {
            return Collections.emptyList();
        }
        return baseMapper.selectSymptomsByExpertiseId(expertiseId);
    }

    @Override
    @Transactional
    public boolean updateExpertiseSymptoms(Integer expertiseId, List<Integer> symptomIds, List<Float> relevanceScores) {
        log.info("Updating symptoms for expertise id: {} with symptomIds: {}", expertiseId, symptomIds);
        
        if (expertiseId == null) {
            log.error("Invalid expertiseId: null");
            return false;
        }

        try {
            // Delete existing associations
            baseMapper.deleteExpertiseSymptoms(expertiseId);
            
            // If symptomIds is empty, we just delete all associations
            if (symptomIds.isEmpty()) {
                return true;
            }
            
            // Insert new associations
            baseMapper.insertExpertiseSymptoms(expertiseId, symptomIds, relevanceScores);
            
            return true;
        } catch (Exception e) {
            log.error("Error updating expertise symptoms: ", e);
            throw e;
        }
    }

    @Override
    public List<SymptomDTO> searchSymptoms(String keyword) {
        log.info("Searching symptoms with keyword: {}", keyword);
        LambdaQueryWrapper<SymptomDTO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(SymptomDTO::getKeyword, keyword)
                   .orderByDesc(SymptomDTO::getSymptomId)
                   .last("LIMIT 10");
        return list(queryWrapper);
    }
    
    @Override
    public List<String> findSimilarKeywords(List<String> keywords) {
        log.info("Finding similar keywords for: {}", keywords);
        if (CollectionUtils.isEmpty(keywords)) {
            return Collections.emptyList();
        }
        
        try {
            return baseMapper.findSimilarKeywords(keywords);
        } catch (Exception e) {
            log.error("Error finding similar keywords: ", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<String> findKeywordsWithFuzzyMatch(List<String> keywords) {
        log.info("Finding keywords with fuzzy match for: {}", keywords);
        if (CollectionUtils.isEmpty(keywords)) {
            return Collections.emptyList();
        }
        
        try {
            return baseMapper.findKeywordsWithFuzzyMatch(keywords);
        } catch (Exception e) {
            log.error("Error finding keywords with fuzzy match: ", e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<Map<String, Object>> getExpertisesBySymptomId(Integer symptomId) {
        log.info("获取症状ID:{}的相关专长", symptomId);
        if (symptomId == null) {
            log.warn("症状ID为空");
            return Collections.emptyList();
        }
        
        try {
            return baseMapper.getExpertisesBySymptomId(symptomId);
        } catch (Exception e) {
            log.error("获取症状关联专长失败: ", e);
            return Collections.emptyList();
        }
    }
} 