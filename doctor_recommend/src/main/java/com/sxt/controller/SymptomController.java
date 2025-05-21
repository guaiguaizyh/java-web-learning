package com.sxt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sxt.pojo.Result;
import com.sxt.pojo.SymptomDTO;
import com.sxt.service.SymptomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 症状管理接口
 */
@Slf4j
@RestController
@RequestMapping("/symptoms")
public class SymptomController {
    @Autowired
    private SymptomService symptomService;

    @GetMapping
    public Result getSymptomList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer departmentId) {
        try {
            log.info("获取症状列表请求，页码：{}，每页大小：{}，关键词：{}，科室ID：{}",
                    page, size, keyword, departmentId);
            IPage<SymptomDTO> result = symptomService.getSymptomList(page, size, keyword, departmentId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取症状列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{symptomId}")
    public Result getSymptomById(@PathVariable Integer symptomId) {
        try {
            log.info("获取症状详情请求，症状ID：{}", symptomId);
            SymptomDTO symptom = symptomService.getSymptomById(symptomId);
            if (symptom == null) {
                return Result.error("症状不存在");
            }
            return Result.success(symptom);
        } catch (Exception e) {
            log.error("获取症状详情失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取症状相关的专长列表
     * @param symptomId 症状ID
     * @return 专长信息列表
     */
    @GetMapping("/{symptomId}/expertises")
    public Result getExpertisesBySymptomId(@PathVariable Integer symptomId) {
        try {
            log.info("获取症状专长请求，症状ID：{}", symptomId);
            List<Map<String, Object>> expertises = symptomService.getExpertisesBySymptomId(symptomId);
            return Result.success(expertises);
        } catch (Exception e) {
            log.error("获取症状专长失败", e);
            return Result.error("获取症状相关专长失败: " + e.getMessage());
        }
    }

    @PostMapping
    public Result addSymptom(@RequestBody SymptomDTO symptom) {
        try {
            log.info("添加症状请求，症状信息：{}", symptom);
            boolean success = symptomService.addSymptom(symptom);
            if (!success) {
                return Result.error("添加症状失败");
            }
            return Result.success(true);
        } catch (IllegalArgumentException e) {
            log.error("添加症状参数错误", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("添加症状失败", e);
            return Result.error("添加症状失败");
        }
    }

    @PutMapping("/{symptomId}")
    public Result updateSymptom(@PathVariable Integer symptomId, @RequestBody SymptomDTO symptom) {
        try {
            // 设置症状ID
            symptom.setSymptomId(symptomId);
            
            log.info("更新症状请求，症状ID：{}，症状信息：{}", symptomId, symptom);
            
            // 验证参数
            if (symptom.getKeyword() == null || symptom.getKeyword().trim().isEmpty()) {
                return Result.error("症状关键词不能为空");
            }
            if (symptom.getKeyword().length() < 2 || symptom.getKeyword().length() > 10) {
                return Result.error("症状关键词长度应在2-10个字符之间");
            }
            if (symptom.getDepartmentId() == null) {
                return Result.error("科室ID不能为空");
            }
            
            boolean success = symptomService.updateSymptom(symptom);
            if (!success) {
                return Result.error("更新症状失败");
            }
            return Result.success(true);
        } catch (Exception e) {
            log.error("更新症状失败", e);
            return Result.error("更新症状失败");
        }
    }

    @DeleteMapping("/{symptomId}")
    public Result deleteSymptom(@PathVariable Integer symptomId) {
        try {
            log.info("删除症状请求，症状ID：{}", symptomId);
            boolean success = symptomService.deleteSymptom(symptomId);
            if (!success) {
                return Result.error("删除症状失败");
            }
            return Result.success(true);
        } catch (IllegalArgumentException e) {
            log.error("删除症状参数错误", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("删除症状失败", e);
            return Result.error("删除症状失败");
        }
    }
    
    /**
     * 查找相似的症状关键词（基础版）
     * @param keywords 逗号分隔的关键词列表
     * @return 找到的相似症状关键词列表
     */
    @GetMapping("/similar")
    public Result findSimilarKeywords(@RequestParam String keywords) {
        try {
            log.info("查找相似症状关键词请求，关键词：{}", keywords);
            if (keywords == null || keywords.trim().isEmpty()) {
                return Result.error("关键词不能为空");
            }
            
            // 将逗号分隔的关键词转换为List
            List<String> keywordList = Arrays.asList(keywords.split(","));
            
            // 调用服务层方法查找相似关键词
            List<String> similarKeywords = symptomService.findSimilarKeywords(keywordList);
            return Result.success(similarKeywords);
        } catch (Exception e) {
            log.error("查找相似症状关键词失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 使用高级模糊匹配查找症状关键词
     * @param keywords 逗号分隔的关键词列表
     * @return 找到的症状关键词列表
     */
    @GetMapping("/fuzzy-match")
    public Result findKeywordsWithFuzzyMatch(@RequestParam String keywords) {
        try {
            log.info("使用模糊匹配查找症状关键词请求，关键词：{}", keywords);
            if (keywords == null || keywords.trim().isEmpty()) {
                return Result.error("关键词不能为空");
            }
            
            // 将逗号分隔的关键词转换为List
            List<String> keywordList = Arrays.asList(keywords.split(","));
            
            // 调用服务层方法进行高级模糊匹配
            List<String> matchedKeywords = symptomService.findKeywordsWithFuzzyMatch(keywordList);
            return Result.success(matchedKeywords);
        } catch (Exception e) {
            log.error("使用模糊匹配查找症状关键词失败", e);
            return Result.error(e.getMessage());
        }
    }
} 