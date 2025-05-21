package com.sxt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sxt.pojo.Expertise;
import com.sxt.pojo.DoctorExpertise;
import com.sxt.pojo.Result;
import com.sxt.service.ExpertiseService;
import com.sxt.service.SymptomService;
import com.sxt.pojo.SymptomDTO;
import com.sxt.service.DoctorService;
import com.sxt.pojo.vo.DoctorVO;
import com.sxt.pojo.Doctor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * 专长管理接口
 */
@Slf4j
@RestController
@RequestMapping("/expertises")
public class ExpertiseController {
    @Autowired
    private ExpertiseService expertiseService;

    @Autowired
    private SymptomService symptomService;

    @Autowired
    private DoctorService doctorService;

    /**
     * 获取专长列表，支持多条件查询和排序
     * @param page 页码
     * @param size 每页大小
     * @param expertiseName 专长名称（模糊查询）
     * @param departmentId 科室ID
     * @param symptomKeyword 关联症状关键词（模糊查询）
     * @param sortBy 排序字段（ID, NAME, DEPARTMENT, SYMPTOM_COUNT, PROFICIENCY, RELEVANCE）
     * @param sortOrder 排序方式（asc, desc）
     * @return 分页结果
     */
    @GetMapping
    public Result getExpertiseList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String expertiseName,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) String symptomKeyword,
            @RequestParam(required = false, defaultValue = "ID") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
        try {
            log.info("获取专长列表请求，页码：{}，每页大小：{}，专长名称：{}，科室ID：{}，症状关键词：{}，排序方式：{}，排序顺序：{}",
                    page, size, expertiseName, departmentId, symptomKeyword, sortBy, sortOrder);
            IPage<Expertise> result = expertiseService.getExpertiseList(page, size, expertiseName, departmentId, 
                                                                    symptomKeyword, sortBy, sortOrder);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取专长列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    @GetMapping("/{expertiseId}")
    public Result getExpertiseById(@PathVariable Integer expertiseId) {
        try {
            log.info("获取专长详情请求，专长ID：{}", expertiseId);
            Expertise expertise = expertiseService.getExpertiseById(expertiseId);
            if (expertise == null) {
                return Result.error("专长不存在");
            }
            return Result.success(expertise);
        } catch (Exception e) {
            log.error("获取专长详情失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    public Result addExpertise(@RequestBody Expertise expertise) {
        try {
            log.info("添加专长请求，专长信息：{}", expertise);
            boolean success = expertiseService.addExpertise(expertise);
            if (!success) {
                return Result.error("添加专长失败");
            }
            return Result.success(true);
        } catch (IllegalArgumentException e) {
            log.error("添加专长参数错误", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("添加专长失败", e);
            return Result.error("添加专长失败");
        }
    }

    @PutMapping("/{expertiseId}")
    public Result updateExpertise(@PathVariable Integer expertiseId, @RequestBody Expertise expertise) {
        try {
            log.info("更新专长请求，专长ID：{}，专长信息：{}", expertiseId, expertise);
            expertise.setExpertiseId(expertiseId);
            boolean success = expertiseService.updateExpertise(expertise);
            if (!success) {
                return Result.error("更新专长失败");
            }
            return Result.success(true);
        } catch (IllegalArgumentException e) {
            log.error("更新专长参数错误", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新专长失败", e);
            return Result.error("更新专长失败");
        }
    }

    @DeleteMapping("/{expertiseId}")
    public Result deleteExpertise(@PathVariable Integer expertiseId) {
        try {
            log.info("删除专长请求，专长ID：{}", expertiseId);
            boolean success = expertiseService.deleteExpertise(expertiseId);
            if (!success) {
                return Result.error("删除专长失败");
            }
            return Result.success(true);
        } catch (IllegalArgumentException e) {
            log.error("删除专长参数错误", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("删除专长失败", e);
            return Result.error("删除专长失败");
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public Result getExpertisesByDoctorId(@PathVariable Integer doctorId) {
        try {
            log.info("获取医生的专长列表请求，医生ID：{}", doctorId);
            List<Expertise> expertises = expertiseService.getExpertisesByDoctorId(doctorId);
            return Result.success(expertises);
        } catch (Exception e) {
            log.error("获取医生的专长列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/doctor/{doctorId}")
    public Result updateDoctorExpertises(@PathVariable Integer doctorId, @RequestBody List<DoctorExpertise> expertises) {
        try {
            log.info("更新医生的专长请求，医生ID：{}，专长列表：{}", doctorId, expertises);
            boolean success = expertiseService.updateDoctorExpertises(doctorId, expertises);
            if (!success) {
                return Result.error("更新医生的专长失败");
            }
            return Result.success(true);
        } catch (IllegalArgumentException e) {
            log.error("更新医生的专长参数错误", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新医生的专长失败", e);
            return Result.error("更新医生的专长失败");
        }
    }

    @GetMapping("/department/{departmentId}")
    public Result getExpertisesByDepartmentId(@PathVariable Integer departmentId) {
        try {
            log.info("获取科室的专长列表请求，科室ID：{}", departmentId);
            List<Expertise> expertises = expertiseService.getExpertisesByDepartmentId(departmentId);
            return Result.success(expertises);
        } catch (Exception e) {
            log.error("获取科室的专长列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取专长关联的症状列表
     */
    @GetMapping("/{expertiseId}/symptoms")
    public Result getExpertiseSymptoms(@PathVariable Integer expertiseId) {
        try {
            log.info("获取专长关联的症状列表请求，专长ID：{}", expertiseId);
            // 先检查专长是否存在
            Expertise expertise = expertiseService.getExpertiseById(expertiseId);
            if (expertise == null) {
                return Result.error("专长不存在");
            }
            
            // 引入SymptomService
            List<SymptomDTO> symptoms = symptomService.getSymptomsByExpertiseId(expertiseId);
            return Result.success(symptoms);
        } catch (Exception e) {
            log.error("获取专长关联的症状列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新专长关联的症状
     */
    @PutMapping("/{expertiseId}/symptoms")
    public Result updateExpertiseSymptoms(
            @PathVariable Integer expertiseId,
            @RequestBody Map<String, Object> requestBody) {
        try {
            log.info("更新专长关联的症状请求，专长ID：{}，请求体：{}", expertiseId, requestBody);
            
            // 先检查专长是否存在
            Expertise expertise = expertiseService.getExpertiseById(expertiseId);
            if (expertise == null) {
                return Result.error("专长不存在");
            }
            
            // 解析请求体中的symptomIds和relevanceScores
            @SuppressWarnings("unchecked")
            List<Integer> symptomIds = (List<Integer>) requestBody.get("symptomIds");
            @SuppressWarnings("unchecked")
            List<Float> relevanceScores = (List<Float>) requestBody.get("relevanceScores");
            
            // 如果symptomIds为空，表示要删除所有关联
            if (symptomIds == null) {
                symptomIds = new ArrayList<>();
            }
            if (relevanceScores == null) {
                relevanceScores = new ArrayList<>();
            }
            
            // 如果提供了症状ID，则验证相关度分数列表
            if (!symptomIds.isEmpty() && (relevanceScores == null || relevanceScores.size() != symptomIds.size())) {
                return Result.error("相关度分数列表必须与症状ID列表长度一致");
            }
            
            boolean success = symptomService.updateExpertiseSymptoms(expertiseId, symptomIds, relevanceScores);
            if (!success) {
                return Result.error("更新专长关联的症状失败");
            }
            
            return Result.success(true);
        } catch (Exception e) {
            log.error("更新专长关联的症状失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取拥有特定专长的医生列表
     * @param expertiseId 专长ID
     * @param page 页码
     * @param size 每页大小
     * @return 医生列表
     */
    @GetMapping("/{expertiseId}/doctors")
    public Result getDoctorsByExpertiseId(
            @PathVariable Integer expertiseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("获取拥有特定专长的医生列表请求，专长ID：{}，页码：{}，每页大小：{}", expertiseId, page, size);
            
            // 先检查专长是否存在
            Expertise expertise = expertiseService.getExpertiseById(expertiseId);
            if (expertise == null) {
                log.error("专长不存在，专长ID：{}", expertiseId);
                return Result.error("专长不存在");
            }
            
            // 调用服务方法获取医生列表
            IPage<DoctorVO> doctors = doctorService.getDoctorsByExpertiseId(expertiseId, page, size);
            log.info("成功获取专长ID={}的医生列表，共{}条记录", expertiseId, doctors.getTotal());
            return Result.success(doctors);
        } catch (Exception e) {
            log.error("获取拥有特定专长的医生列表失败，专长ID：{}，错误：{}", expertiseId, e.getMessage(), e);
            return Result.error("获取拥有专长的医生列表失败：" + e.getMessage());
        }
    }

    /**
     * 更新专长关联的医生列表（包括新增、更新和删除）
     * @param expertiseId 专长ID
     * @param doctors 医生列表
     * @return 更新结果
     */
    @PutMapping("/{expertiseId}/doctors")
    public Result updateExpertiseDoctors(
            @PathVariable Integer expertiseId,
            @RequestBody List<DoctorExpertise> doctors) {
        try {
            log.info("更新专长的医生列表，专长ID：{}，医生列表：{}", expertiseId, doctors);
            
            // 先检查专长是否存在
            Expertise expertise = expertiseService.getExpertiseById(expertiseId);
            if (expertise == null) {
                return Result.error("专长不存在");
            }
            
            // 验证每个医生的proficiency值是否合法
            for (DoctorExpertise de : doctors) {
                if (de.getProficiency() == null || de.getProficiency() < 0 || de.getProficiency() > 1) {
                    return Result.error("熟练度必须在0-1之间");
                }
                // 设置expertiseId
                de.setExpertiseId(expertiseId);
            }
            
            boolean success = expertiseService.updateExpertiseDoctors(expertiseId, doctors);
            return success ? Result.success(true) : Result.error("更新失败");
        } catch (Exception e) {
            log.error("更新专长的医生列表失败", e);
            return Result.error(e.getMessage());
        }
    }
} 