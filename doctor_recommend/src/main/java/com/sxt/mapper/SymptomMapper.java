package com.sxt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sxt.pojo.SymptomDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SymptomMapper extends BaseMapper<SymptomDTO> {
    /**
     * 分页查询症状列表
     */
    IPage<SymptomDTO> selectSymptomPage(Page<?> page, @Param("keyword") String keyword, @Param("departmentId") Integer departmentId);
    
    /**
     * 根据专长ID获取相关症状
     */
    List<SymptomDTO> selectSymptomsByExpertiseId(@Param("expertiseId") Integer expertiseId);
    
    /**
     * 删除专长关联的所有症状
     */
    int deleteExpertiseSymptoms(@Param("expertiseId") Integer expertiseId);
    
    /**
     * 批量插入专长-症状关联
     */
    int insertExpertiseSymptoms(@Param("expertiseId") Integer expertiseId, 
                               @Param("symptomIds") List<Integer> symptomIds,
                               @Param("relevanceScores") List<Float> relevanceScores);
    
    /**
     * 根据输入的关键词列表查找相似的症状关键词
     * @param keywords 关键词列表
     * @return 找到的相似症状关键词列表
     */
    List<String> findSimilarKeywords(@Param("keywords") List<String> keywords);
    
    /**
     * 根据输入的关键词列表进行高级模糊匹配查找症状关键词
     * 支持同义词、部分匹配和模式匹配等
     * @param keywords 关键词列表
     * @return 找到的症状关键词列表
     */
    List<String> findKeywordsWithFuzzyMatch(@Param("keywords") List<String> keywords);
    
    /**
     * 获取所有症状信息
     * @return 所有症状信息列表
     */
    List<Map<String, Object>> getAllSymptoms();
    
    /**
     * 根据症状ID获取相关专长信息
     * @param symptomId 症状ID
     * @return 关联的专长信息列表，包含专长ID、名称和关联度
     */
    List<Map<String, Object>> getExpertisesBySymptomId(@Param("symptomId") Integer symptomId);
} 