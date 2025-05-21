package com.sxt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sxt.pojo.RecommendedDoctorDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 医生推荐数据访问层
 * 包含症状匹配、专长匹配和医生推荐的相关算法
 */
@Mapper
public interface DoctorRecommendMapper extends BaseMapper<RecommendedDoctorDTO> {
    Logger log = LoggerFactory.getLogger(DoctorRecommendMapper.class);

    /**
     * 根据关键词匹配症状，并获取相关专长信息
     * @param keywords 关键词列表
     * @return 匹配结果列表，包含症状ID、专长ID等信息
     */
    List<Map<String, Object>> matchSymptomsByKeywords(@Param("keywords") List<String> keywords);
    
    /**
     * 根据专长ID列表获取医生信息
     * @param expertiseIds 专长ID列表
     * @return 医生信息列表
     */
    List<RecommendedDoctorDTO> getDoctorsByExpertises(@Param("expertiseIds") List<Integer> expertiseIds);
    
    /**
     * 获取评分最高的医生列表
     * @param limit 返回数量限制
     * @return 医生列表
     */
    List<RecommendedDoctorDTO> getTopRatedDoctors(@Param("limit") int limit);
    
    /**
     * 获取所有医生列表（用于备选推荐）
     * @param limit 返回数量限制
     * @return 医生列表
     */
    List<RecommendedDoctorDTO> getAllDoctors(@Param("limit") int limit);
}
