package com.sxt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sxt.mapper.DepartmentMapper;
import com.sxt.mapper.DoctorMapper;
import com.sxt.mapper.DoctorExpertiseMapper;
import com.sxt.mapper.ExpertiseMapper;
import com.sxt.pojo.Department;
import com.sxt.pojo.Doctor;
import com.sxt.pojo.DoctorExpertise;
import com.sxt.pojo.Expertise;
import com.sxt.service.DoctorService;
import com.sxt.service.ExpertiseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ExpertiseServiceImpl extends ServiceImpl<ExpertiseMapper, Expertise> implements ExpertiseService {

    @Autowired
    private ExpertiseMapper expertiseMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DoctorExpertiseMapper doctorExpertiseMapper;

    @Override
    public IPage<Expertise> getExpertiseList(int page, int size, String expertiseName, Integer departmentId, String symptomKeyword, String sortBy, String sortOrder) {
        log.info("获取专长列表，页码：{}，每页大小：{}，专长名称：{}，科室ID：{}，症状关键词：{}，排序方式：{}，排序顺序：{}", 
                page, size, expertiseName, departmentId, symptomKeyword, sortBy, sortOrder);
                
        // 1. 创建分页参数
        Page<Expertise> pageParam = new Page<>(page, size);
        
        // 2. 转换排序方式为数据库可识别的字段
        String dbSortField;
        switch (sortBy.toUpperCase()) {
            case "NAME":
                dbSortField = "e.expertise_name";
                break;
            case "DEPARTMENT":
                dbSortField = "d.department_name";
                break;
            case "SYMPTOM_COUNT":
                dbSortField = "symptom_count";
                break;
            case "PROFICIENCY":
                dbSortField = "avg_proficiency";
                break;
            case "RELEVANCE":
                dbSortField = "relevance_score";
                break;
            case "ID":
            default:
                dbSortField = "e.expertise_id"; // 默认按ID排序
        }
        
        // 3. 转换排序顺序
        boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
        
        // 4. 执行查询
        return expertiseMapper.getExpertiseListWithFiltersAndSort(pageParam, expertiseName, departmentId, 
                                                      symptomKeyword, sortBy, dbSortField, isAsc);
    }

    @Override
    public Expertise getExpertiseById(Integer expertiseId) {
        log.info("获取专长详情，专长ID：{}", expertiseId);
        return expertiseMapper.selectById(expertiseId);
    }

    @Override
    @Transactional
    public boolean addExpertise(Expertise expertise) {
        log.info("添加专长，专长信息：{}", expertise);
        // 验证专长名称是否已存在
        if (expertiseMapper.checkExpertiseNameExists(expertise.getExpertiseName(), null) > 0) {
            log.error("专长名称已存在：{}", expertise.getExpertiseName());
            throw new IllegalArgumentException("专长名称已存在");
        }

        // 如果指定了科室ID，验证科室是否存在
        if (expertise.getDepartmentId() != null) {
        Department department = departmentMapper.selectById(expertise.getDepartmentId());
        if (department == null) {
            log.error("科室不存在，科室ID：{}", expertise.getDepartmentId());
            throw new IllegalArgumentException("科室不存在");
        }
        }

        return expertiseMapper.insert(expertise) > 0;
    }

    @Override
    @Transactional
    public boolean updateExpertise(Expertise expertise) {
        log.info("更新专长，专长信息：{}", expertise);
        // 验证专长名称是否已存在（排除自身）
        if (expertiseMapper.checkExpertiseNameExists(expertise.getExpertiseName(), expertise.getExpertiseId()) > 0) {
            log.error("专长名称已存在：{}", expertise.getExpertiseName());
            throw new IllegalArgumentException("专长名称已存在");
        }

        // 如果指定了科室ID，验证科室是否存在
        if (expertise.getDepartmentId() != null) {
        Department department = departmentMapper.selectById(expertise.getDepartmentId());
        if (department == null) {
            log.error("科室不存在，科室ID：{}", expertise.getDepartmentId());
            throw new IllegalArgumentException("科室不存在");
        }
        }

        // 使用自定义的update方法而不是updateById，确保可以更新null值
        return expertiseMapper.updateExpertise(expertise) > 0;
    }

    @Override
    @Transactional
    public boolean deleteExpertise(Integer expertiseId) {
        log.info("删除专长，专长ID：{}", expertiseId);
        
        // 1. 验证专长是否存在
        Expertise expertise = expertiseMapper.selectById(expertiseId);
        if (expertise == null) {
            log.error("专长不存在，专长ID：{}", expertiseId);
            throw new IllegalArgumentException("专长不存在");
        }

        try {
            // 直接删除专长，依赖数据库的外键级联删除来处理关联关系
            int result = expertiseMapper.deleteById(expertiseId);
            log.info("删除专长结果：{}", result > 0);
            return result > 0;
        } catch (Exception e) {
            log.error("删除专长失败", e);
            throw e;
        }
    }

    @Override
    public List<Expertise> getExpertisesByDoctorId(Integer doctorId) {
        log.info("获取医生的专长列表，医生ID：{}", doctorId);
        return expertiseMapper.getExpertisesByDoctorId(doctorId);
    }

    @Override
    @Transactional
    public boolean updateDoctorExpertises(Integer doctorId, List<DoctorExpertise> expertises) {
        log.info("更新医生的专长，医生ID：{}，专长列表：{}", doctorId, expertises);
        
        // 1. 验证医生是否存在
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            log.error("医生不存在，医生ID：{}", doctorId);
            throw new IllegalArgumentException("医生不存在");
        }

        // 2. 验证所有专长是否存在
        for (DoctorExpertise expertise : expertises) {
            Expertise existingExpertise = expertiseMapper.selectById(expertise.getExpertiseId());
            if (existingExpertise == null) {
                log.error("专长不存在，专长ID：{}", expertise.getExpertiseId());
                throw new IllegalArgumentException("专长不存在");
            }
            
            // 如果专长和医生都属于某个科室，确保它们属于同一个科室
            if (existingExpertise.getDepartmentId() != null && doctor.getDepartmentId() != null 
                && !existingExpertise.getDepartmentId().equals(doctor.getDepartmentId())) {
                log.error("专长不属于医生所在科室，医生ID：{}，专长ID：{}", doctorId, expertise.getExpertiseId());
                throw new IllegalArgumentException("专长不属于医生所在科室");
            }
        }

        // 3. 验证专长数据的完整性
        for (DoctorExpertise expertise : expertises) {
            if (expertise.getExpertiseId() == null || expertise.getProficiency() == null) {
                log.error("医生专长关联信息不完整：{}", expertise);
                throw new IllegalArgumentException("医生专长关联信息不完整");
            }
        }

        // 4. 删除原有专长关联
        expertiseMapper.deleteDoctorExpertises(doctorId, null);

        // 5. 添加新的专长关联
        if (!expertises.isEmpty()) {
            expertiseMapper.saveDoctorExpertises(doctorId, expertises);
        }

        return true;
    }

    @Override
    public List<Expertise> getExpertisesByDepartmentId(Integer departmentId) {
        log.info("获取科室的专长列表，科室ID：{}", departmentId);
        return expertiseMapper.getExpertisesByDepartmentId(departmentId);
    }

    @Override
    public boolean isExpertiseNameExists(String expertiseName, Integer expertiseId) {
        return expertiseMapper.checkExpertiseNameExists(expertiseName, expertiseId) > 0;
    }

    @Override
    public boolean isDepartmentExists(Integer departmentId) {
        return departmentMapper.selectById(departmentId) != null;
    }

    @Override
    public boolean isDoctorInDepartment(Integer doctorId, Integer departmentId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        return doctor != null && doctor.getDepartmentId().equals(departmentId);
    }

    @Override
    public boolean isDoctorExpertiseExists(Integer doctorId, Integer expertiseId) {
        return expertiseMapper.checkDoctorExpertiseExists(doctorId, expertiseId) > 0;
    }

    @Override
    public boolean addDoctorExpertise(DoctorExpertise doctorExpertise) {
        if (doctorExpertise == null || doctorExpertise.getDoctorId() == null 
            || doctorExpertise.getExpertiseId() == null || doctorExpertise.getProficiency() == null) {
            throw new IllegalArgumentException("医生专长关联信息不完整");
        }
        
        return expertiseMapper.saveDoctorExpertise(
            doctorExpertise.getDoctorId(),
            doctorExpertise.getExpertiseId(),
            doctorExpertise.getProficiency()
        ) > 0;
    }

    @Override
    public Expertise getById(Integer expertiseId) {
        if (expertiseId == null) {
            return null;
        }
        return baseMapper.selectById(expertiseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateExpertiseDoctors(Integer expertiseId, List<DoctorExpertise> doctors) {
        try {
            // 1. 验证专长是否存在
            Expertise expertise = getExpertiseById(expertiseId);
            if (expertise == null) {
                throw new IllegalArgumentException("专长不存在：" + expertiseId);
            }
            
            // 2. 验证所有医生是否属于同一科室
            if (doctors != null && !doctors.isEmpty()) {
                for (DoctorExpertise de : doctors) {
                    Doctor doctor = doctorMapper.selectById(de.getDoctorId());
                    if (doctor == null) {
                        throw new IllegalArgumentException("医生不存在：" + de.getDoctorId());
                    }
                    if (!expertise.getDepartmentId().equals(doctor.getDepartmentId())) {
                        throw new IllegalArgumentException("医生 " + de.getDoctorId() + " 与专长不属于同一科室");
                    }
                }
                
                // 3. 使用UPSERT语句保存或更新关联
                expertiseMapper.saveDoctorsByExpertise(expertiseId, doctors);
            } else {
                // 4. 如果医生列表为空，删除所有关联
                expertiseMapper.deleteDoctorExpertises(null, expertiseId);
            }
            
            return true;
        } catch (Exception e) {
            log.error("更新专长的医生列表失败", e);
            throw e;
        }
    }
} 