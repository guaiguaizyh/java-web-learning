package com.sxt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sxt.pojo.Expertise;
import com.sxt.pojo.DoctorExpertise;

import java.util.List;

public interface ExpertiseService {
    /**
     * 获取专长列表
     * @param page 页码
     * @param size 每页大小
     * @param expertiseName 专长名称（模糊搜索）
     * @param departmentId 科室ID
     * @param symptomKeyword 症状关键词（模糊搜索）
     * @param sortBy 排序方式（可选值：ID、NAME、DEPARTMENT、SYMPTOM_COUNT、PROFICIENCY、RELEVANCE）
     * @param sortOrder 排序顺序（可选值：asc、desc）
     * @return 分页结果
     */
    IPage<Expertise> getExpertiseList(int page, int size, String expertiseName, Integer departmentId, String symptomKeyword, String sortBy, String sortOrder);

    /**
     * 获取专长详情
     * @param expertiseId 专长ID
     * @return 专长信息
     */
    Expertise getExpertiseById(Integer expertiseId);

    /**
     * 添加专长
     * @param expertise 专长信息
     * @return 是否成功
     */
    boolean addExpertise(Expertise expertise);

    /**
     * 更新专长
     * @param expertise 专长信息
     * @return 是否成功
     */
    boolean updateExpertise(Expertise expertise);

    /**
     * 删除专长
     * @param expertiseId 专长ID
     * @return 是否成功
     */
    boolean deleteExpertise(Integer expertiseId);

    /**
     * 获取医生的专长列表（包含熟练度）
     * @param doctorId 医生ID
     * @return 专长列表
     */
    List<Expertise> getExpertisesByDoctorId(Integer doctorId);

    /**
     * 更新医生的专长（包含熟练度）
     * @param doctorId 医生ID
     * @param expertises 专长列表（包含熟练度）
     * @return 是否成功
     */
    boolean updateDoctorExpertises(Integer doctorId, List<DoctorExpertise> expertises);

    /**
     * 获取科室的专长列表
     * @param departmentId 科室ID
     * @return 专长列表
     */
    List<Expertise> getExpertisesByDepartmentId(Integer departmentId);

    /**
     * 验证专长名称是否已存在
     * @param expertiseName 专长名称
     * @param expertiseId 专长ID（更新时使用）
     * @return 是否已存在
     */
    boolean isExpertiseNameExists(String expertiseName, Integer expertiseId);

    /**
     * 验证科室是否存在
     * @param departmentId 科室ID
     * @return 是否存在
     */
    boolean isDepartmentExists(Integer departmentId);

    /**
     * 验证医生是否属于指定科室
     * @param doctorId 医生ID
     * @param departmentId 科室ID
     * @return 是否属于
     */
    boolean isDoctorInDepartment(Integer doctorId, Integer departmentId);

    /**
     * 检查医生是否已关联某专长
     * @param doctorId 医生ID
     * @param expertiseId 专长ID
     * @return 是否已存在关联
     */
    boolean isDoctorExpertiseExists(Integer doctorId, Integer expertiseId);

    /**
     * 添加医生专长关联
     * @param doctorExpertise 医生专长关联信息
     * @return 是否添加成功
     */
    boolean addDoctorExpertise(DoctorExpertise doctorExpertise);

    /**
     * 根据ID获取专长信息
     * @param expertiseId 专长ID
     * @return 专长信息
     */
    Expertise getById(Integer expertiseId);

    /**
     * 更新专长关联的医生列表（包括新增、更新和删除）
     * @param expertiseId 专长ID
     * @param doctors 医生列表
     * @return 是否更新成功
     */
    boolean updateExpertiseDoctors(Integer expertiseId, List<DoctorExpertise> doctors);
} 