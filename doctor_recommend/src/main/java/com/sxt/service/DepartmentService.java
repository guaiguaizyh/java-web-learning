package com.sxt.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sxt.pojo.Department;

import java.util.Map;

public interface DepartmentService {
    /**
     * 获取科室列表（分页）
     * @param page 页码
     * @param size 每页大小
     * @param departmentName 科室名称（可选，模糊匹配）
     * @param description 科室描述（可选，模糊匹配）
     * @param sortField 排序字段（可选，支持：departmentName, doctorCount, averageRating, expertiseCount）
     * @param sortOrder 排序方向（可选，支持：asc, desc）
     * @return 分页后的科室列表
     */
    IPage<Department> getDepartmentList(int page, int size, String departmentName, String description, String sortField, String sortOrder);
    Department getDepartmentById(Integer departmentId);
    boolean addDepartment(Department department);
    boolean updateDepartment(Department department);
    boolean deleteDepartment(Integer departmentId);
    
    /**
     * 删除科室（可选是否级联删除医生）
     * @param departmentId 科室ID
     * @param cascadeDeleteDoctors 是否级联删除医生
     * @return 删除结果
     */
    boolean deleteDepartment(Integer departmentId, boolean cascadeDeleteDoctors);
    
    /**
     * 高级查询科室列表，支持多条件筛选和排序
     * @param page 页码
     * @param size 每页大小
     * @param params 查询参数
     *   - departmentName 科室名称，模糊匹配（可选）
     *   - description 科室描述，模糊匹配（可选）
     *   - minDoctorCount 最小医生数量（可选）
     *   - maxDoctorCount 最大医生数量（可选）
     *   - minRating 最低评分（可选）
     *   - maxRating 最高评分（可选）
     *   - sortField 排序字段（可选，支持departmentId, departmentName, doctorCount, averageRating）
     *   - sortOrder 排序方向（可选，支持asc, desc）
     * @return 分页查询结果
     */
    IPage<Department> advancedSearchDepartments(int page, int size, Map<String, Object> params);
} 