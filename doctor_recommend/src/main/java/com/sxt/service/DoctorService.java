package com.sxt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sxt.pojo.Doctor;
import com.sxt.pojo.RecommendedDoctorDTO;
import com.sxt.pojo.vo.DoctorVO;

import java.util.List;
import java.util.Map;

public interface DoctorService extends IService<Doctor> {
    IPage<Doctor> getDoctorList(int page, int size, String name, Integer departmentId);
    Doctor getDoctorById(Integer doctorId);
    boolean addDoctor(Doctor doctor);
    boolean updateDoctor(Doctor doctor);
    boolean deleteDoctor(Integer doctorId);

    /**
     * 更新医生的科室关联
     * @param doctorId 医生ID
     * @param departmentId 科室ID（null表示取消科室关联）
     * @return 更新结果
     */
    boolean updateDoctorDepartment(Integer doctorId, Integer departmentId);

    /**
     * 根据科室 ID 列表查找推荐医生 (返回 DTO)
     * @param departmentIds 科室 ID 列表
     * @return RecommendedDoctorDTO 列表
     */
    List<RecommendedDoctorDTO> findRecommendedDoctors(List<Integer> departmentIds);

    /**
     * 查找评分最高的 Top N 医生 (返回 DTO)
     * @param limit 数量限制
     * @return RecommendedDoctorDTO 列表
     */
    List<RecommendedDoctorDTO> findTopRatedDoctors(int limit);

    /**
     * 获取医生分页列表 (后台管理用, 可能返回 POJO 或 VO)
     * 如果需要标签，应返回 DoctorVO
     * @param page 分页对象
     * @param name 姓名查询条件
     * @param departmentId 科室查询条件
     * @param title 职称ID查询条件 (positions_id)
     * @param gender 性别编码查询条件
     * @param sortField 排序字段（可选，支持：doctorId, name, averageRating, ratingCount, workYears）
     * @param sortOrder 排序方向（可选，支持：asc, desc）
     * @return IPage<DoctorVO> 医生视图对象的分页列表
     */
    IPage<DoctorVO> getDoctorVoPage(Page<DoctorVO> page, String name, Integer departmentId, Integer positionsId, String gender, String sortField, String sortOrder);

    /**
     * 根据医生 ID 获取医生详细信息 (用于API返回)
     * @param doctorId 医生 ID
     * @return DoctorVO 包含标签和详情，或 null 如果未找到
     */
    DoctorVO getDoctorDetailById(Integer doctorId);
    
    /**
     * 根据科室名称查询医生
     * @param department 科室名称（模糊匹配）
     * @param limit 返回结果限制
     * @return 医生列表
     */
    List<Map<String, Object>> findDoctorsByDepartment(String department, int limit);

    /**
     * 根据科室ID获取医生列表（分页）
     * @param departmentId 科室ID
     * @param page 页码
     * @param size 每页大小
     * @return 医生分页列表
     */
    IPage<DoctorVO> getDoctorsByDepartment(Integer departmentId, int page, int size);

    /**
     * 根据专长ID获取医生列表（分页）
     * @param expertiseId 专长ID
     * @param page 页码
     * @param size 每页大小
     * @return 医生分页列表
     */
    IPage<DoctorVO> getDoctorsByExpertiseId(Integer expertiseId, int page, int size);
} 