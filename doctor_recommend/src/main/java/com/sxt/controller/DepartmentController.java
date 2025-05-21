package com.sxt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sxt.pojo.Department;
import com.sxt.pojo.Result;
import com.sxt.pojo.vo.DoctorVO;
import com.sxt.service.DepartmentService;
import com.sxt.service.DoctorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 科室管理接口
 */
@RestController
@RequestMapping("/departments")
@CrossOrigin(origins = "*") // 允许跨域访问
public class DepartmentController {
    private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);

    @Autowired
    private DepartmentService departmentService;
    
    @Autowired
    private DoctorService doctorService;

    /**
     * 获取科室列表
     * @param page 页码
     * @param size 每页大小
     * @param departmentName 科室名称（可选，模糊匹配）
     * @param description 科室描述（可选，模糊匹配）
     * @param sortField 排序字段（可选，支持：departmentName, doctorCount, averageRating, expertiseCount）
     * @param sortOrder 排序方向（可选，支持：asc, desc）
     * @return 分页后的科室列表
     */
    @GetMapping("/list")
    public Result<IPage<Department>> getDepartmentList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        
        logger.info("获取科室列表请求：page={}, size={}, departmentName={}, description={}, sortField={}, sortOrder={}", 
                page, size, departmentName, description, sortField, sortOrder);
        
        try {
            IPage<Department> result = departmentService.getDepartmentList(
            page, size, departmentName, description, sortField, sortOrder);
            
            logger.info("获取科室列表成功，总记录数：{}", result.getTotal());
            return Result.success(result);
        } catch (Exception e) {
            logger.error("获取科室列表失败", e);
            return Result.error("获取科室列表失败：" + e.getMessage());
        }
    }

    /**
     * 高级查询科室列表，支持多条件筛选和排序
     */
    @GetMapping("/search")
    public Result<IPage<Department>> advancedSearchDepartments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
        
        logger.info("API接收到高级查询科室列表请求：page={}, size={}, departmentName={}, description={}, " +
                "sortField={}, sortOrder={}", 
                page, size, departmentName, description, sortField, sortOrder);
        
        try {
            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            if (departmentName != null && !departmentName.trim().isEmpty()) {
                params.put("departmentName", departmentName);
            }
            if (description != null && !description.trim().isEmpty()) {
                params.put("description", description);
            }
            
            // 排序参数总是提供
            params.put("sortField", sortField);
            params.put("sortOrder", sortOrder);
            
            // 调用服务层方法
            IPage<Department> result = departmentService.advancedSearchDepartments(page, size, params);
            logger.info("API高级查询结果：{}", result);
            
            return Result.success(result);
        } catch (Exception e) {
            logger.error("API高级查询科室列表失败", e);
            return Result.error("查询科室列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/{departmentId}")
    public Result<Department> getDepartmentById(@PathVariable Integer departmentId) {
        logger.info("获取科室详情请求：departmentId={}", departmentId);
        try {
            Department department = departmentService.getDepartmentById(departmentId);
            if (department == null) {
                logger.warn("获取科室详情失败：未找到科室");
                return Result.error("未找到指定科室");
            }
            logger.info("获取科室详情响应：{}", department);
            return Result.success(department);
        } catch (Exception e) {
            logger.error("获取科室详情异常：{}", e.getMessage());
            return Result.error("获取科室详情失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result<Boolean> addDepartment(@RequestBody Department department) {
        logger.info("添加科室请求 - 原始数据：{}", department);
        try {
            // 打印接收到的具体字段值
            logger.info("科室名称: '{}', 描述: '{}'", 
                department.getDepartmentName(),
                department.getDescription());

            // 验证科室对象
            Department.validateDepartment(department);

            // 确保科室名称已正确处理
            String departmentName = department.getDepartmentName().trim();
            department.setDepartmentName(departmentName);
            
            logger.info("开始添加科室 - 处理后的数据：{}", department);
            boolean success = departmentService.addDepartment(department);
            logger.info("添加科室完成 - 结果：{}", success);
            return Result.success(success);
        } catch (IllegalArgumentException e) {
            logger.warn("添加科室参数验证失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            logger.error("添加科室异常：", e);
            return Result.error("添加科室失败：" + e.getMessage());
        }
    }

    @PutMapping("/{departmentId}")
    public Result<Boolean> updateDepartment(
            @PathVariable Integer departmentId,
            @RequestBody Department department) {
        logger.info("更新科室请求：departmentId={}, department={}", departmentId, department);
        try {
            // 先获取原有科室信息
            Department existingDepartment = departmentService.getDepartmentById(departmentId);
            if (existingDepartment == null) {
                logger.warn("更新科室失败：科室不存在");
                return Result.error("科室不存在");
            }

            // 只更新不为null的字段
            if (department.getDepartmentName() != null) {
                existingDepartment.setDepartmentName(department.getDepartmentName());
            }
            if (department.getDescription() != null) {
                existingDepartment.setDescription(department.getDescription());
            }

            // 验证更新后的数据
            if (existingDepartment.getDepartmentName() == null || existingDepartment.getDepartmentName().trim().isEmpty()) {
                logger.warn("更新科室失败：科室名称为空");
                return Result.error("科室名称不能为空");
            }

            boolean success = departmentService.updateDepartment(existingDepartment);
            logger.info("更新科室响应：{}", success);
            return Result.success(success);
        } catch (Exception e) {
            logger.error("更新科室异常：{}", e.getMessage());
            return Result.error("更新科室失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{departmentId}")
    public Result<Boolean> deleteDepartment(@PathVariable Integer departmentId) {
        logger.info("删除科室请求：departmentId={}", departmentId);
        try {
            boolean success = departmentService.deleteDepartment(departmentId);
            logger.info("删除科室响应：{}", success);
            return Result.success(success);
        } catch (Exception e) {
            logger.error("删除科室异常：{}", e.getMessage());
            return Result.error("删除科室失败：" + e.getMessage());
        }
    }

    /**
     * 获取科室下的医生列表
     * @param departmentId 科室ID
     * @param page 页码
     * @param size 每页大小
     * @return 医生列表分页结果
     */
    @GetMapping("/{departmentId}/doctors")
    public Result<IPage<DoctorVO>> getDepartmentDoctors(
            @PathVariable Integer departmentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("获取科室医生列表请求：departmentId={}, page={}, size={}", departmentId, page, size);
        try {
            IPage<DoctorVO> doctors = doctorService.getDoctorsByDepartment(departmentId, page, size);
            logger.info("获取科室医生列表成功，总记录数：{}", doctors.getTotal());
            return Result.success(doctors);
        } catch (Exception e) {
            logger.error("获取科室医生列表失败", e);
            return Result.error("获取科室医生列表失败：" + e.getMessage());
        }
    }
} 