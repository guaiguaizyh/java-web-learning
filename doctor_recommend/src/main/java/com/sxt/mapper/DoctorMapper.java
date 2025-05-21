package com.sxt.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.sxt.pojo.Doctor;
import com.sxt.pojo.RecommendedDoctorDTO;
import com.sxt.pojo.vo.DoctorVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 医生数据访问接口
 * 继承MyBatis-Plus的BaseMapper，提供基础的CRUD操作
 */
@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {
    /**
     * 查询医生列表（带科室和专长, 返回 POJO, 使用 BaseResultMap）
     * @param ew 查询条件包装器
     * @return Doctor 列表
     */
    List<Doctor> selectDoctorList(@Param(Constants.WRAPPER) Wrapper<Doctor> ew);

    /**
     * 查询医生列表（带关联科室和专长）
     * @param page 分页参数
     * @param wrapper 查询条件
     * @return 分页的医生列表
     */
    IPage<Doctor> selectDoctorList(IPage<Doctor> page, @Param(Constants.WRAPPER) QueryWrapper<Doctor> wrapper);

    /**
     * 根据科室 ID 列表查询医生信息，并按评分排序，返回 DTO 列表
     * @param departmentIds 科室 ID 列表
     * @return 推荐医生 DTO 列表
     */
    List<RecommendedDoctorDTO> findDoctorsInDepartmentsOrderedByRating(@Param("departmentIds") List<Integer> departmentIds);

    /**
     * 查询评分最高的 Top N 医生，返回 DTO 列表
     * @param limit 返回数量
     * @return Top N 医生 DTO 列表
     */
    List<RecommendedDoctorDTO> findTopRatedDoctors(@Param("limit") int limit);

    /**
     * 根据医生 ID 查询医生视图对象 (包含标签和所需详情)
     * XML 中 id="selectDoctorVoById"
     * @param doctorId 医生 ID
     * @return DoctorVO 对象，或 null 如果未找到
     */
    DoctorVO selectDoctorVoById(@Param("doctorId") Integer doctorId);

    /**
     * 查询医生视图对象列表 (用于需要标签的列表展示，如后台管理)
     * XML 中 id="selectDoctorVoList"
     * @param name 医生姓名 (查询条件)
     * @param departmentId 科室 ID (查询条件)
     * @param positionsId 职称编码 (查询条件)
     * @param gender 性别编码 (查询条件)
     * @param sortField 排序字段（可选，支持：doctorId, name, averageRating）
     * @param sortOrder 排序方向（可选，支持：asc, desc）
     * @return DoctorVO 列表
     */
    List<DoctorVO> selectDoctorVoList(
            @Param("name") String name,
            @Param("departmentId") Integer departmentId,
            @Param("positionsId") Integer positionsId,
            @Param("gender") String gender,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder
    );

    /**
     * 根据科室名称查询医生（模糊匹配），按评分降序排序
     * @param department 科室名称关键词
     * @param limit 返回数量限制
     * @return 医生列表
     */
    List<Doctor> selectDoctorsByDepartmentName(@Param("department") String department, @Param("limit") int limit);

    /**
     * 根据科室ID查询医生视图对象列表（分页）
     * @param page 分页参数
     * @param departmentId 科室ID
     * @return 分页的医生视图对象列表
     */
    IPage<DoctorVO> selectDoctorVoByDepartmentId(IPage<DoctorVO> page, @Param("departmentId") Integer departmentId);
    
    /**
     * 根据专长ID查询医生视图对象列表（分页）
     * @param page 分页参数
     * @param expertiseId 专长ID
     * @return 分页的医生视图对象列表
     */
    IPage<DoctorVO> selectDoctorVoByExpertiseId(IPage<DoctorVO> page, @Param("expertiseId") Integer expertiseId);

    /**
     * 根据关键词查询相关医生
     * @param keywords 症状关键词列表
     * @return 医生信息列表
     */
    List<Map<String, Object>> findDoctorsByKeywords(@Param("keywords") List<String> keywords);

    /**
     * 获取所有科室名称
     * @return 科室名称列表
     */
    @Select("SELECT DISTINCT d.department_name FROM departments d ORDER BY d.department_id")
    List<String> getAllDepartmentNames();

    /**
     * 清除医生的职称关联
     * 将指定职称ID的医生的positions_id设为null
     * @param positionsId 职称ID
     * @return 受影响的记录数
     */
    @Update("UPDATE doctors SET positions_id = NULL WHERE positions_id = #{positionsId}")
    int clearDoctorPositions(@Param("positionsId") Integer positionsId);
    
    /**
     * 获取使用指定职称的医生数量
     * @param positionsId 职称ID
     * @return 使用该职称的医生数量
     */
    @Select("SELECT COUNT(*) FROM doctors WHERE positions_id = #{positionsId}")
    int countDoctorsByPositionsId(@Param("positionsId") Integer positionsId);

    /**
     * 更新医生的科室关联
     * @param doctorId 医生ID
     * @param departmentId 科室ID
     * @return 受影响的记录数
     */
    @Update("UPDATE doctors SET department_id = #{departmentId} WHERE doctor_id = #{doctorId}")
    int updateDepartmentId(@Param("doctorId") Integer doctorId, @Param("departmentId") Integer departmentId);

    /**
     * 根据科室名称查询医生详细信息
     * @param departmentName 科室名称
     * @param limit 限制数量
     * @return 医生详细信息列表
     */
    List<Map<String, Object>> findDoctorsByDepartmentWithDetails(
        @Param("departmentName") String departmentName, 
        @Param("limit") int limit
    );
}
