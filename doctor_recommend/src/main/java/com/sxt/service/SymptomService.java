package com.sxt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sxt.pojo.SymptomDTO;

import java.util.List;
import java.util.Map;

public interface SymptomService {
    /**
     * 获取症状列表
     * @param page 页码
     * @param size 每页大小
     * @param keyword 关键词（模糊搜索）
     * @param departmentId 科室ID
     * @return 分页结果
     */
    IPage<SymptomDTO> getSymptomList(int page, int size, String keyword, Integer departmentId);

    /**
     * 获取症状详情
     * @param symptomId 症状ID
     * @return 症状信息
     */
    SymptomDTO getSymptomById(Integer symptomId);

    /**
     * 添加症状
     * @param symptom 症状信息
     * @return 是否成功
     */
    boolean addSymptom(SymptomDTO symptom);

    /**
     * 更新症状
     * @param symptom 症状信息
     * @return 是否成功
     */
    boolean updateSymptom(SymptomDTO symptom);

    /**
     * 删除症状
     * @param symptomId 症状ID
     * @return 是否成功
     */
    boolean deleteSymptom(Integer symptomId);
    
    /**
     * 获取专长相关的症状列表
     * @param expertiseId 专长ID
     * @return 症状列表
     */
    List<SymptomDTO> getSymptomsByExpertiseId(Integer expertiseId);
    
    /**
     * 更新专长关联的症状
     * @param expertiseId 专长ID
     * @param symptomIds 症状ID列表
     * @param relevanceScores 相关度分数列表，与symptomIds顺序一致
     * @return 是否成功
     */
    boolean updateExpertiseSymptoms(Integer expertiseId, List<Integer> symptomIds, List<Float> relevanceScores);
    
    /**
     * 根据关键词搜索症状
     * @param keyword 搜索关键词
     * @return 匹配的症状列表
     */
    List<SymptomDTO> searchSymptoms(String keyword);
    
    /**
     * 根据输入的关键词列表查找相似的症状关键词
     * @param keywords 关键词列表
     * @return 找到的相似症状关键词列表
     */
    List<String> findSimilarKeywords(List<String> keywords);
    
    /**
     * 根据输入的关键词列表进行高级模糊匹配查找症状关键词
     * 支持部分匹配和分词匹配等
     * @param keywords 关键词列表
     * @return 找到的症状关键词列表
     */
    List<String> findKeywordsWithFuzzyMatch(List<String> keywords);
    
    /**
     * 根据症状ID获取关联的专长信息
     * @param symptomId 症状ID
     * @return 专长信息列表，包含专长ID、名称和关联度
     */
    List<Map<String, Object>> getExpertisesBySymptomId(Integer symptomId);
} 