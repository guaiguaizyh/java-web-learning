package com.sxt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sxt.pojo.Expertise;
import com.sxt.pojo.DoctorExpertise;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

@Mapper
public interface ExpertiseMapper extends BaseMapper<Expertise> {
    /**
     * 根据医生ID获取专长列表（包含熟练度）
     * @param doctorId 医生ID
     * @return 专长列表
     */
    List<Expertise> getExpertisesByDoctorId(@Param("doctorId") Integer doctorId);

    /**
     * 保存医生的专长（包含熟练度）
     * @param doctorId 医生ID
     * @param expertises 专长列表（包含熟练度）
     */
    void saveDoctorExpertises(@Param("doctorId") Integer doctorId, @Param("expertises") List<DoctorExpertise> expertises);

    /**
     * 更新医生专长熟练度
     * @param doctorId 医生ID
     * @param expertiseId 专长ID
     * @param proficiency 熟练度
     * @return 影响行数
     */
    int updateDoctorExpertiseProficiency(@Param("doctorId") Integer doctorId, 
                                       @Param("expertiseId") Integer expertiseId, 
                                       @Param("proficiency") Float proficiency);

    /**
     * 删除医生的专长
     * @param doctorId 医生ID
     * @param expertiseId 专长ID（可选，为null时删除所有专长）
     */
    void deleteDoctorExpertises(@Param("doctorId") Integer doctorId, @Param("expertiseId") Integer expertiseId);

    /**
     * 检查专长名称是否存在
     * @param expertiseName 专长名称
     * @param expertiseId 专长ID（更新时使用，排除自身）
     * @return 存在的数量
     */
    int checkExpertiseNameExists(@Param("expertiseName") String expertiseName, @Param("expertiseId") Integer expertiseId);

    /**
     * 获取专长列表
     * @param expertiseName 专长名称
     * @param departmentId 科室ID
     * @return 专长列表
     */
    List<Expertise> getExpertiseList(@Param("expertiseName") String expertiseName, @Param("departmentId") Integer departmentId);

    /**
     * 获取科室的专长列表
     * @param departmentId 科室ID
     * @return 专长列表
     */
    List<Expertise> getExpertisesByDepartmentId(@Param("departmentId") Integer departmentId);

    /**
     * 根据ID获取专长信息
     * @param expertiseId 专长ID
     * @return 专长信息
     */
    Expertise getExpertiseById(@Param("expertiseId") Integer expertiseId);

    /**
     * 插入新的专长
     * @param expertise 专长信息
     * @return 影响行数
     */
    int insertExpertise(Expertise expertise);

    /**
     * 更新专长信息
     * @param expertise 专长信息
     * @return 影响行数
     */
    int updateExpertise(Expertise expertise);

    /**
     * 删除专长
     * @param expertiseId 专长ID
     * @return 影响行数
     */
    int deleteExpertise(@Param("expertiseId") Integer expertiseId);

    /**
     * 获取所有医生的专长数据
     * @return 医生专长列表
     */
    List<DoctorExpertise> getAllDoctorExpertises();

    /**
     * 根据专长名称列表查找专长ID列表
     * @param expertiseNames 专长名称列表
     * @return 专长ID列表
     */
    List<Integer> findExpertiseIdsByNames(@Param("expertiseNames") List<String> expertiseNames);

    /**
     * 获取专长列表（支持症状搜索和排序）
     * @param page 分页参数
     * @param expertiseName 专长名称
     * @param departmentId 科室ID
     * @param symptomKeyword 症状关键词
     * @param sortBy 排序方式
     * @param sortField 排序字段
     * @param isAsc 是否升序
     * @return 分页结果
     */
    IPage<Expertise> getExpertiseListWithFiltersAndSort(
        Page<Expertise> page,
        @Param("expertiseName") String expertiseName,
        @Param("departmentId") Integer departmentId,
        @Param("symptomKeyword") String symptomKeyword,
        @Param("sortBy") String sortBy,
        @Param("sortField") String sortField,
        @Param("isAsc") boolean isAsc
    );

    @Select("SELECT COUNT(*) FROM doctor_expertise WHERE doctor_id = #{doctorId} AND expertise_id = #{expertiseId}")
    int checkDoctorExpertiseExists(@Param("doctorId") Integer doctorId, @Param("expertiseId") Integer expertiseId);

    @Insert("INSERT INTO doctor_expertise (doctor_id, expertise_id, proficiency, create_time) VALUES (#{doctorId}, #{expertiseId}, #{proficiency}, NOW())")
    int saveDoctorExpertise(@Param("doctorId") Integer doctorId, @Param("expertiseId") Integer expertiseId, @Param("proficiency") Float proficiency);

    /**
     * 批量保存医生专长关联（从专长角度）
     * @param expertiseId 专长ID
     * @param doctors 医生列表
     */
    void saveDoctorsByExpertise(@Param("expertiseId") Integer expertiseId, @Param("doctors") List<DoctorExpertise> doctors);

    /**
     * 批量保存医生专长关联（从医生角度）
     */

    /**
     * 根据专长名称查询专长ID
     * @param expertiseName 专长名称
     * @return 专长ID
     */
    @Select("SELECT expertise_id FROM expertises WHERE expertise_name = #{expertiseName}")
    Integer getExpertiseIdByName(String expertiseName);

    /**
     * 删除专长的症状关联
     * @param expertiseId 专长ID
     */
    void deleteSymptomExpertises(@Param("expertiseId") Integer expertiseId);
} 