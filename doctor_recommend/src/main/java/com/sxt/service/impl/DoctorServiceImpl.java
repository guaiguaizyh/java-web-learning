package com.sxt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sxt.mapper.DepartmentMapper;
import com.sxt.mapper.DoctorMapper;
import com.sxt.pojo.Department;
import com.sxt.pojo.Doctor;
import com.sxt.pojo.RecommendedDoctorDTO;
import com.sxt.pojo.vo.DoctorVO;
import com.sxt.service.DoctorService;
import com.sxt.service.DoctorReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {
    private static final Logger logger = LoggerFactory.getLogger(DoctorServiceImpl.class);

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private DoctorReviewService doctorReviewService;

    @Override
    public IPage<Doctor> getDoctorList(int page, int size, String name, Integer departmentId) {
        try {
            Page<Doctor> pageParam = new Page<>(page, size);
            QueryWrapper<Doctor> wrapper = new QueryWrapper<>();
            
            if (name != null && !name.isEmpty()) {
                wrapper.like("name", name);
            }
            if (departmentId != null) {
                wrapper.eq("department_id", departmentId);
            }
            
            return doctorMapper.selectDoctorList(pageParam, wrapper);
        } catch (Exception e) {
            logger.error("获取医生列表失败", e);
            throw e;
        }
    }

    @Override
    public Doctor getDoctorById(Integer doctorId) {
        try {
            if (doctorId == null || doctorId <= 0) {
                logger.error("医生ID无效: {}", doctorId);
                throw new IllegalArgumentException("医生ID无效");
            }
            return doctorMapper.selectById(doctorId);
        } catch (Exception e) {
            logger.error("获取医生详情失败, doctorId: {}", doctorId, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean addDoctor(Doctor doctor) {
        try {
            validateDoctor(doctor);
            return this.save(doctor);
        } catch (Exception e) {
            logger.error("添加医生失败", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean updateDoctor(Doctor doctor) {
        try {
            if (doctor == null || doctor.getDoctorId() == null || doctor.getDoctorId() <= 0) {
                logger.error("更新医生信息失败：医生ID无效");
                throw new IllegalArgumentException("医生ID无效");
            }

            // 检查医生是否存在
            Doctor existingDoctor = doctorMapper.selectById(doctor.getDoctorId());
            if (existingDoctor == null) {
                logger.error("更新医生信息失败：医生不存在, doctorId: {}", doctor.getDoctorId());
                throw new IllegalArgumentException("医生不存在");
            }

            // 验证更新的数据
            validateDoctor(doctor);

            // 保留原有的非空值
            if (doctor.getName() == null) {
                doctor.setName(existingDoctor.getName());
            }
            if (doctor.getGender() == null) {
                doctor.setGender(existingDoctor.getGender());
            }
            if (doctor.getAge() == null) {
                doctor.setAge(existingDoctor.getAge());
            }
            if (doctor.getPositionsId() == null) {
                doctor.setPositionsId(existingDoctor.getPositionsId());
            }
            if (doctor.getDepartmentId() == null) {
                doctor.setDepartmentId(existingDoctor.getDepartmentId());
            }
            if (doctor.getAvatarUrl() == null) {
                doctor.setAvatarUrl(existingDoctor.getAvatarUrl());
            }
            if (doctor.getAverageRating() == null) {
                doctor.setAverageRating(existingDoctor.getAverageRating());
            }
            if (doctor.getRatingCount() == null) {
                doctor.setRatingCount(existingDoctor.getRatingCount());
            }
            if (doctor.getWorkYears() == null) {
                doctor.setWorkYears(existingDoctor.getWorkYears());
            }

            return this.updateById(doctor);
        } catch (Exception e) {
            logger.error("更新医生信息失败, doctorId: {}", doctor.getDoctorId(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean deleteDoctor(Integer doctorId) {
        logger.info("删除医生，ID: {}", doctorId);
        
        // 先检查医生是否存在
        Doctor doctor = getById(doctorId);
        if (doctor == null) {
            logger.warn("要删除的医生不存在，ID: {}", doctorId);
            return false;
        }
        
        try {
            // 1. 先删除医生的所有评价
            logger.info("删除医生之前，先删除与医生关联的所有评价");
            doctorReviewService.deleteReviewsByDoctorId(doctorId);
            
            // 2. 然后删除医生记录
            boolean result = removeById(doctorId);
            logger.info("医生删除结果: {}, ID: {}", result, doctorId);
            return result;
        } catch (Exception e) {
            logger.error("删除医生时发生错误，ID: {}", doctorId, e);
            throw new RuntimeException("删除医生失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public boolean updateDoctorDepartment(Integer doctorId, Integer departmentId) {
        return lambdaUpdate()
                .eq(Doctor::getDoctorId, doctorId)
                .set(Doctor::getDepartmentId, departmentId)
                .update();
    }

    /**
     * 验证医生信息
     * @param doctor 医生信息
     */
    private void validateDoctor(Doctor doctor) {
        if (doctor == null) {
            throw new IllegalArgumentException("医生信息不能为空");
        }
        if (doctor.getName() == null || doctor.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("医生姓名不能为空");
        }
        if (doctor.getDepartmentId() == null || doctor.getDepartmentId() <= 0) {
            throw new IllegalArgumentException("科室ID无效");
        }
        if (doctor.getAge() != null && doctor.getAge() <= 0) {
            throw new IllegalArgumentException("年龄必须大于0");
        }
        if (doctor.getGender() != null && !doctor.getGender().matches("^[男女]$")) {
            throw new IllegalArgumentException("性别只能是'男'或'女'");
        }
        if (doctor.getRatingCount() != null && doctor.getRatingCount() < 0) {
            throw new IllegalArgumentException("评分数量不能为负数");
        }
        if (doctor.getWorkYears() != null && doctor.getWorkYears() < 0) {
            throw new IllegalArgumentException("工作年限不能为负数");
        }
    }

    @Override
    public List<RecommendedDoctorDTO> findRecommendedDoctors(List<Integer> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return List.of();
        }
        return doctorMapper.findDoctorsInDepartmentsOrderedByRating(departmentIds);
    }

    @Override
    public List<RecommendedDoctorDTO> findTopRatedDoctors(int limit) {
        return doctorMapper.findTopRatedDoctors(limit);
    }

    @Override
    public IPage<DoctorVO> getDoctorVoPage(Page<DoctorVO> page, String name, Integer departmentId, Integer positionsId, String gender, String sortField, String sortOrder) {
        try {
            logger.info("分页查询医生列表: page={}, size={}, name={}, departmentId={}, positionsId={}, gender={}, sortField={}, sortOrder={}",
                    page.getCurrent(), page.getSize(), name, departmentId, positionsId, gender, sortField, sortOrder);
            
            // 设置默认排序
            if (sortField == null || sortField.isEmpty()) {
                sortField = "doctorId";
            }
            if (sortOrder == null || sortOrder.isEmpty()) {
                sortOrder = "asc";
            }
            
            // 查询符合条件的医生列表
            List<DoctorVO> records = doctorMapper.selectDoctorVoList(name, departmentId, positionsId, gender, sortField, sortOrder);
            
            // 手动分页处理
            long total = records.size();
            long size = page.getSize();
            long current = page.getCurrent();
            long pages = (total + size - 1) / size;
            
            // 计算当前页的记录范围
            int fromIndex = (int) ((current - 1) * size);
            int toIndex = (int) Math.min(fromIndex + size, total);
            
            // 返回当前页的记录
            List<DoctorVO> pageRecords = fromIndex < total 
                    ? records.subList(fromIndex, toIndex) 
                    : List.of();
            
            // 设置分页信息
            Page<DoctorVO> resultPage = new Page<>(current, size, total, pages == 0);
            resultPage.setRecords(pageRecords);
            
            return resultPage;
        } catch (Exception e) {
            logger.error("分页查询医生列表失败", e);
            throw e;
        }
    }

    @Override
    public DoctorVO getDoctorDetailById(Integer doctorId) {
        return doctorMapper.selectDoctorVoById(doctorId);
    }

    @Override
    public List<Map<String, Object>> findDoctorsByDepartment(String departmentName, int limit) {
        logger.info("根据科室名称查询医生，科室: {}, 限制: {}", departmentName, limit);
        
        // 使用正确的mapper方法
        List<Doctor> doctors = doctorMapper.selectDoctorsByDepartmentName(departmentName, limit);
        
        // 转换结果为Map格式，确保字段名与AI客服期望的格式一致
        return doctors.stream().map(doctor -> {
            Map<String, Object> result = new HashMap<>();
            result.put("doctor_id", doctor.getDoctorId());
            result.put("name", doctor.getName());
            result.put("gender", doctor.getGender());
            result.put("averageRating", doctor.getAverageRating());
            result.put("ratingCount", doctor.getRatingCount());
            result.put("departmentName", doctor.getDepartmentName());
            result.put("positionsName", doctor.getPositionsName());
            result.put("workYears", doctor.getWorkYears());
            result.put("expertiseList", doctor.getExpertiseList());
            result.put("avatarUrl", doctor.getAvatarUrl());
            return result;
        }).collect(Collectors.toList());
    }

    @Override
    public IPage<DoctorVO> getDoctorsByDepartment(Integer departmentId, int page, int size) {
        if (departmentId == null || departmentId <= 0) {
            logger.error("科室ID无效: {}", departmentId);
            throw new IllegalArgumentException("科室ID无效");
        }
        
        Page<DoctorVO> pageParam = new Page<>(page, size);
        return doctorMapper.selectDoctorVoByDepartmentId(pageParam, departmentId);
    }

    @Override
    public IPage<DoctorVO> getDoctorsByExpertiseId(Integer expertiseId, int page, int size) {
        if (expertiseId == null || expertiseId <= 0) {
            logger.error("专长ID无效: {}", expertiseId);
            throw new IllegalArgumentException("专长ID无效");
        }
        
        // 检查专长是否存在
        try {
            // 此处可以添加检查专长是否存在的逻辑，但是这不是必须的
            // 因为Controller层已经做了这个检查
            
            logger.info("查询专长ID={}的医生列表，页码={}，每页数量={}", expertiseId, page, size);
            Page<DoctorVO> pageParam = new Page<>(page, size);
            return doctorMapper.selectDoctorVoByExpertiseId(pageParam, expertiseId);
        } catch (Exception e) {
            logger.error("查询专长ID={}的医生列表失败: {}", expertiseId, e.getMessage(), e);
            throw new RuntimeException("查询医生列表失败: " + e.getMessage(), e);
        }
    }
}