package com.sxt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sxt.mapper.DepartmentMapper;
import com.sxt.pojo.Department;
import com.sxt.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public IPage<Department> getDepartmentList(int page, int size, String departmentName, String description, String sortField, String sortOrder) {
        log.info("查询科室列表参数: page={}, size={}, departmentName={}, description={}, sortField={}, sortOrder={}", 
                page, size, departmentName, description, sortField, sortOrder);
        
        // 获取原始数据
        List<Department> departments = departmentMapper.selectDepartmentList(departmentName, description, sortField, sortOrder);
        log.info("查询到的原始数据：{}", departments);

        // 手动分页
        Page<Department> pageResult = new Page<>(page, size);
        int total = departments.size();
        int start = (int)((page - 1) * size);
        int end = Math.min(start + size, total);
        
        if (start < total) {
            pageResult.setRecords(departments.subList(start, end));
        }
        pageResult.setTotal(total);
        
        log.info("分页后的数据：{}", pageResult.getRecords());
        
        return pageResult;
    }

    @Override
    public Department getDepartmentById(Integer departmentId) {
        log.info("获取科室详情：departmentId={}", departmentId);
        Department department = departmentMapper.selectById(departmentId);
        log.info("获取科室详情结果：{}", department);
        return department;
    }

    @Override
    @Transactional
    public boolean addDepartment(Department department) {
        log.info("添加科室：{}", department);
        // 检查科室名称是否已存在
        int count = departmentMapper.checkDepartmentNameExists(department.getDepartmentName(), null);
        if (count > 0) {
            log.warn("添加科室失败：科室名称已存在");
            throw new RuntimeException("已有对应科室");
        }
        int result = departmentMapper.insert(department);
        boolean success = result > 0;
        log.info("添加科室结果：{}", success);
        return success;
    }

    @Override
    @Transactional
    public boolean updateDepartment(Department department) {
        log.info("更新科室开始：{}", department);
        try {
            // 检查科室名称是否已存在（排除自身）
            int count = departmentMapper.checkDepartmentNameExists(department.getDepartmentName(), department.getDepartmentId());
            if (count > 0) {
                log.warn("更新科室失败：科室名称已存在");
                throw new RuntimeException("已有对应科室");
            }
            
            // 执行更新
            int result = departmentMapper.updateById(department);
            boolean success = result > 0;
            log.info("更新科室结果：success={}, department={}", success, department);
            return success;
        } catch (Exception e) {
            log.error("更新科室异常：", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean deleteDepartment(Integer departmentId) {
        // 调用新的删除方法，默认不级联删除医生
        return deleteDepartment(departmentId, false);
    }

    @Override
    @Transactional
    public boolean deleteDepartment(Integer departmentId, boolean cascadeDeleteDoctors) {
        log.info("删除科室：departmentId={}, cascadeDeleteDoctors={}", departmentId, cascadeDeleteDoctors);
        
        // 检查科室下是否有医生
        int doctorCount = departmentMapper.checkDoctorsExist(departmentId);
        if (doctorCount > 0 && !cascadeDeleteDoctors) {
            log.warn("删除科室失败：科室下还有{}个医生", doctorCount);
            throw new RuntimeException("该科室下还有" + doctorCount + "个医生，请先处理医生或选择级联删除");
        }

        // 如果需要级联删除医生
        if (cascadeDeleteDoctors && doctorCount > 0) {
            log.info("删除科室下的{}个医生", doctorCount);
            departmentMapper.deleteDoctorsByDepartmentId(departmentId);
        }

        // 删除科室
        int result = departmentMapper.deleteById(departmentId);
        boolean success = result > 0;
        log.info("删除科室结果：{}", success);
        return success;
    }

    @Override
    public IPage<Department> advancedSearchDepartments(int page, int size, Map<String, Object> params) {
        log.info("开始高级查询科室列表：page={}, size={}, params={}", page, size, params);
        
        // 从params中提取参数
        String departmentName = params.get("departmentName") != null ? 
                params.get("departmentName").toString() : null;
                
        String description = params.get("description") != null ? 
                params.get("description").toString() : null;
                
        String sortField = params.get("sortField") != null ? 
                params.get("sortField").toString() : "departmentId";
                
        String sortOrder = params.get("sortOrder") != null ? 
                params.get("sortOrder").toString() : "asc";
        
        // 验证排序字段
        List<String> validSortFields = List.of("departmentId", "departmentName", "doctorCount", "averageRating");
        if (!validSortFields.contains(sortField)) {
            log.warn("无效的排序字段: {}，使用默认排序字段: departmentId", sortField);
            sortField = "departmentId";
        }
        
        // 验证排序方向
        List<String> validSortOrders = List.of("asc", "desc");
        if (!validSortOrders.contains(sortOrder)) {
            log.warn("无效的排序方向: {}，使用默认排序方向: asc", sortOrder);
            sortOrder = "asc";
        }
        
        try {
            // 查询数据
            List<Department> departments = departmentMapper.advancedSelectDepartmentList(
                    departmentName, description, sortField, sortOrder);
            
            log.info("查询到的原始数据数量：{}", departments != null ? departments.size() : 0);
            
            // 如果查询结果为空，返回空分页对象
            if (departments == null || departments.isEmpty()) {
                log.warn("未查询到科室数据");
                return new Page<>();
            }
            
            // 计算分页信息
            Page<Department> pageParam = new Page<>(page, size);
            long total = departments.size();
            long current = pageParam.getCurrent();
            long pageSize = pageParam.getSize();
            
            // 手动分页
            int fromIndex = (int)((current - 1) * pageSize);
            int toIndex = (int) Math.min(fromIndex + pageSize, total);
            
            List<Department> pageRecords = fromIndex < total 
                ? departments.subList(fromIndex, toIndex) 
                : new ArrayList<>();
            
            log.info("分页后的数据：{}", pageRecords);
            
            // 设置分页结果
            Page<Department> resultPage = new Page<>(current, pageSize, total);
            resultPage.setRecords(pageRecords);
            
            return resultPage;
        } catch (Exception e) {
            log.error("高级查询科室列表异常：", e);
            throw new RuntimeException("查询科室列表失败", e);
        }
    }
} 