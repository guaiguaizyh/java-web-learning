package com.sxt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sxt.pojo.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
    /**
     * 查询科室列表
     * @param departmentName 科室名称（可选，模糊匹配）
     * @param description 科室描述（可选，模糊匹配）
     * @param sortField 排序字段（可选，支持：departmentName, doctorCount, averageRating）
     * @param sortOrder 排序方向（可选，支持：asc, desc）
     * @return 科室列表
     */
    List<Department> selectDepartmentList(
            @Param("departmentName") String departmentName,
            @Param("description") String description,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder);
    
    /**
     * 高级查询科室列表，支持多条件筛选和排序
     * @param departmentName 科室名称，模糊匹配（可选）
     * @param description 科室描述，模糊匹配（可选）
     * @param sortField 排序字段（departmentId, departmentName, doctorCount, averageRating）
     * @param sortOrder 排序方向（asc, desc）
     * @return 科室列表
     */
    List<Department> advancedSelectDepartmentList(
            @Param("departmentName") String departmentName,
            @Param("description") String description,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder);

    /**
     * 检查科室下是否有医生
     * @param departmentId 科室ID
     * @return 医生数量
     */
    int checkDoctorsExist(@Param("departmentId") Integer departmentId);

    /**
     * 检查科室名称是否已存在
     * @param departmentName 科室名称
     * @param departmentId 科室ID（更新时使用，排除自身）
     * @return 存在的数量
     */
    int checkDepartmentNameExists(@Param("departmentName") String departmentName, @Param("departmentId") Integer departmentId);

    /**
     * 删除科室下的所有医生
     * @param departmentId 科室ID
     * @return 删除的记录数
     */
    int deleteDoctorsByDepartmentId(@Param("departmentId") Integer departmentId);
} 