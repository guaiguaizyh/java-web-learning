package com.sxt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sxt.pojo.Doctor;
import com.sxt.pojo.Positions;
import com.sxt.pojo.Result;
import com.sxt.service.DoctorService;
import com.sxt.service.PositionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.sxt.pojo.vo.DoctorVO;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 医生管理接口
 */
@Slf4j
@RestController
@RequestMapping("/doctors")
public class DoctorController {
    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private PositionsService positionsService;

    @GetMapping
    public Result<IPage<DoctorVO>> getDoctorList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer positionsId,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false, defaultValue = "doctorId") String sortField,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
        try {
            log.info("查询医生列表, page:{}, size:{}, name:{}, departmentId:{}, positionsId:{}, gender:{}, sortField:{}, sortOrder:{}",
                    page, size, name, departmentId, positionsId, gender, sortField, sortOrder);
            
            Page<DoctorVO> pageParam = new Page<>(page, size);
            IPage<DoctorVO> result = doctorService.getDoctorVoPage(pageParam, name, departmentId, positionsId, gender, sortField, sortOrder);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取医生列表失败", e);
            return Result.error("获取医生列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取医生职称列表
     * @return 职称列表
     */
    @GetMapping("/positions")
    public Result<List<Positions>> getDoctorPositions() {
        try {
            log.info("获取医生职称列表");
            List<Positions> positionsList = positionsService.getAllPositions();
            return Result.success(positionsList);
        } catch (Exception e) {
            log.error("获取医生职称列表失败", e);
            return Result.error("获取医生职称列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取医生职称列表 (兼容旧版前端)
     * @return 职称列表，使用旧版数据格式
     */
    @GetMapping("/titles")
    public Result<List<Map<String, Object>>> getDoctorTitles() {
        try {
            log.info("获取医生职称列表(旧版格式)");
            List<Positions> positionsList = positionsService.getAllPositions();
            List<Map<String, Object>> titleList = new ArrayList<>();
            
            for (Positions position : positionsList) {
                Map<String, Object> item = new HashMap<>();
                item.put("code", position.getPositionsId().toString());
                item.put("label", position.getPositionsName());
                item.put("value", position.getPositionsId().toString());
                titleList.add(item);
            }
            
            return Result.success(titleList);
        } catch (Exception e) {
            log.error("获取医生职称列表失败", e);
            return Result.error("获取医生职称列表失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result<Boolean> addDoctor(@RequestBody Doctor doctor) {
        try {
            if (doctor.getName() == null || doctor.getName().trim().isEmpty()) {
                return Result.error("医生姓名不能为空");
            }
            if (doctor.getDepartmentId() == null) {
                return Result.error("科室ID不能为空");
            }
            return Result.success(doctorService.addDoctor(doctor));
        } catch (Exception e) {
            return Result.error("添加医生失败：" + e.getMessage());
        }
    }

    //修改医生信息
    //数据回显
    @GetMapping("/{doctorId}")
    public Result<Doctor> getDoctorById(@PathVariable Integer doctorId) {
        log.info("查询回显：{}",doctorId);
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor == null) {
            return Result.error("医生不存在");
        }
        return Result.success(doctor);
    }

    /**
     * 更新医生信息
     * 支持部分字段更新，只更新请求体中包含的字段
     */
    @PutMapping("/{doctorId}")
    public Result<Boolean> updateDoctor(
            @PathVariable Integer doctorId,
            @RequestBody Doctor doctor) {
        log.info("修改医生信息：{}",doctor);
        try {
            // 如果请求包含name字段，则验证不能为空
            if (doctor.getName() != null && doctor.getName().trim().isEmpty()) {
                return Result.error("医生姓名不能为空");
            }
            
            doctor.setDoctorId(doctorId);
            return Result.success(doctorService.updateDoctor(doctor));
        } catch (Exception e) {
            return Result.error("更新医生信息失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{doctorId}")
    public Result<Boolean> deleteDoctor(@PathVariable Integer doctorId) {
        try {
            return Result.success(doctorService.deleteDoctor(doctorId));
        } catch (Exception e) {
            return Result.error("删除医生失败：" + e.getMessage());
        }
    }

    /**
     * 更新医生科室关联
     * @param doctorId 医生ID
     * @param request 包含 departmentId 的请求体
     * @return 更新结果
     */
    @PutMapping("/{doctorId}/department")
    public Result<Boolean> updateDoctorDepartment(
            @PathVariable Integer doctorId,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer departmentId = request.get("departmentId");
            log.info("更新医生科室关联：doctorId={}, departmentId={}", doctorId, departmentId);
            return Result.success(doctorService.updateDoctorDepartment(doctorId, departmentId));
        } catch (Exception e) {
            log.error("更新医生科室关联失败", e);
            return Result.error("更新医生科室关联失败：" + e.getMessage());
        }
    }
}
