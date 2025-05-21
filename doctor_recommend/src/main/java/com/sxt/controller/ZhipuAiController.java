package com.sxt.controller;

import com.sxt.pojo.Result;
import com.sxt.pojo.dto.ChatMessage;
import com.sxt.pojo.RecommendedDoctorDTO;
import com.sxt.service.DoctorService;
import com.sxt.service.ZhipuAiService;
import com.sxt.service.DoctorRecommendService;
import com.sxt.mapper.DoctorMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 智谱AI聊天控制器
 */
@RestController
@RequestMapping("/ai")
@Slf4j
@CrossOrigin(origins = "*")
public class ZhipuAiController {
    
    @Autowired
    private ZhipuAiService zhipuAiService;
    
    @Autowired
    private DoctorService doctorService;
    
    @Autowired
    private DoctorRecommendService doctorRecommendService;
    
    @Autowired
    private DoctorMapper doctorMapper;
    
    /**
     * 统一的AI聊天入口
     * @param request 包含用户消息的请求体
     * @return AI回复的消息或推荐结果
     */
    @PostMapping("/chat")
    public Result<?> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        String lastContext = request.getOrDefault("lastContext", "");
        
        if (userMessage.trim().isEmpty()) {
            return Result.error("消息内容不能为空");
        }
        
        log.info("收到用户聊天消息: {}, 上下文: {}", userMessage, lastContext);
        
        try {
            Map<String, Object> responseData = new HashMap<>();
            String aiResponse;
            
            // 根据上下文状态处理对话
            if (lastContext.isEmpty()) {
                // 首次对话，记录主要症状，同时询问持续时间和伴随症状
                aiResponse = "您好，很抱歉听到您不舒服。为了更好地帮助您，请问：\n" +
                           "1. 这些症状持续多久了？\n" +
                           "2. 是否还有其他不适或伴随症状（如头晕、恶心、发热等）？\n" +
                           "请尽可能详细地告诉我，这对诊断很重要。";
                responseData.put("context", "SYMPTOMS:" + userMessage);
            } 
            else if (lastContext.startsWith("SYMPTOMS:")) {
                // 已有症状信息，直接进行分析和建议
                String symptoms = lastContext.substring("SYMPTOMS:".length());
                
                // 调用智谱AI进行分析和推荐
                String[] userDetails = userMessage.split("[,，;；。.]"); // 分割用户补充描述
                StringBuilder allSymptoms = new StringBuilder();
                allSymptoms.append("患者症状描述：\n");
                allSymptoms.append("1. 主要症状：").append(symptoms).append("\n");
                
                // 解析持续时间
                String duration = "";
                String otherSymptoms = "";
                for (String detail : userDetails) {
                    if (detail.contains("天") || detail.contains("周") || detail.contains("月") || 
                        detail.contains("年") || detail.contains("小时")) {
                        duration = detail.trim();
                    } else {
                        if (!detail.trim().isEmpty()) {
                            otherSymptoms += detail.trim() + "，";
                        }
                    }
                }
                
                if (!duration.isEmpty()) {
                    allSymptoms.append("2. 持续时间：").append(duration).append("\n");
                }
                if (!otherSymptoms.isEmpty()) {
                    allSymptoms.append("3. 伴随症状：").append(otherSymptoms.substring(0, otherSymptoms.length() - 1)).append("\n");
                }
                
                // 添加提示让AI更好地理解
                allSymptoms.append("\n请综合以上所有症状进行分析，给出最合适的就诊科室建议。");
                
                // 1. 获取科室推荐
                String departmentRecommendation = zhipuAiService.recommendDepartment(allSymptoms.toString());
                List<String> departments = extractDepartments(departmentRecommendation);
                String reason = extractReason(departmentRecommendation);
                
                // 2. 生成关怀和建议信息
                StringBuilder recommendation = new StringBuilder();
                recommendation.append("感谢您的详细描述。让我来为您分析一下：\n\n");
                recommendation.append("根据您的情况：\n");
                recommendation.append("- 主要症状：").append(symptoms).append("\n");
                if (!duration.isEmpty()) {
                    recommendation.append("- 持续时间：").append(duration).append("\n");
                }
                if (!otherSymptoms.isEmpty()) {
                    recommendation.append("- 伴随症状：").append(otherSymptoms.substring(0, otherSymptoms.length() - 1)).append("\n");
                }
                recommendation.append("\n");
                
                // 添加一些生活建议
                recommendation.append("💡 在就医前，建议您：\n");
                recommendation.append("1. 保持充足的休息，避免过度劳累\n");
                recommendation.append("2. 规律作息，保证睡眠质量\n");
                recommendation.append("3. 记录症状变化，以便向医生详细描述\n\n");
                
                // 添加科室推荐和理由
                recommendation.append("👨‍⚕️ 建议您到以下科室就诊：\n");
                for (String dept : departments) {
                    recommendation.append("- ").append(dept).append("\n");
                }
                recommendation.append("\n理由：").append(reason).append("\n\n");
                
                recommendation.append("是否需要我为您推荐相关科室的医生？");
                
                aiResponse = recommendation.toString();
                // 保存第一个科室作为默认推荐
                String primaryDepartment = departments.isEmpty() ? "" : String.join("、", departments);
                responseData.put("context", "RECOMMEND:" + primaryDepartment + ";" + allSymptoms.toString());
                responseData.put("departments", departments); // 保存所有推荐科室
                responseData.put("type", "department_recommendation");
            }
            else if (lastContext.startsWith("RECOMMEND:")) {
                // 处理是否需要医生推荐的回复
                String[] parts = lastContext.substring("RECOMMEND:".length()).split(";", 2);
                String departmentNames = parts[0];  // 现在包含所有科室，用、分隔
                String allSymptoms = parts[1];
                
                // 检查用户是否确认需要推荐医生
                if (userMessage.toLowerCase().contains("是") || 
                    userMessage.toLowerCase().contains("好") || 
                    userMessage.toLowerCase().contains("需要") || 
                    userMessage.toLowerCase().contains("可以")) {
                    
                    // 获取所有推荐科室的医生
                    List<String> departments = Arrays.asList(departmentNames.split("、"));
                    log.info("准备查询以下科室的医生: {}", departments);
                    int doctorPerDept = 2;
                    List<Map<String, Object>> allDoctors = new ArrayList<>();

                    for (String dept : departments) {
                        log.info("查询{}的医生", dept);
                        List<Map<String, Object>> doctors = doctorService.findDoctorsByDepartment(dept.trim(), doctorPerDept);
                        for (Map<String, Object> doc : doctors) {
                            doc.put("departmentName", dept.trim()); // 确保有科室字段
                            allDoctors.add(doc);
                        }
                    }

                    StringBuilder doctorRecommendation = new StringBuilder();
                    if (!allDoctors.isEmpty()) {
                        doctorRecommendation.append("根据您的症状，为您推荐以下科室的专家：\n\n");
                        String currentDept = "";
                        for (Map<String, Object> doctor : allDoctors) {
                            String deptName = (String) doctor.get("departmentName");
                            if (!deptName.equals(currentDept)) {
                                doctorRecommendation.append("\n【").append(deptName).append("】\n");
                                currentDept = deptName;
                            }
                            doctorRecommendation.append("👨‍⚕️ ").append(doctor.get("name")).append("\n");
                            doctorRecommendation.append("⭐ 职称：").append(doctor.get("positionsName")).append("\n");
                            doctorRecommendation.append("💫 专长：").append(doctor.get("expertiseList")).append("\n");
                            doctorRecommendation.append("⭐ 评分：").append(doctor.get("averageRating"))
                                .append(" (").append(doctor.get("ratingCount")).append("条评价)\n\n");
                        }
                        doctorRecommendation.append("💡 温馨提示：\n");
                        doctorRecommendation.append("1. 以上信息仅供参考，建议您到医院相关科室就医\n");
                        doctorRecommendation.append("2. 具体出诊时间请以医院公示为准\n");
                        doctorRecommendation.append("3. 如遇急症，请立即前往医院急诊科就医");
                    } else {
                        doctorRecommendation.append("抱歉，当前没有找到相关科室的医生信息，建议您：\n\n");
                        doctorRecommendation.append("1. 直接前往医院相关科室就诊\n");
                        doctorRecommendation.append("2. 向医院咨询相关科室的专家门诊时间\n");
                        doctorRecommendation.append("3. 如遇急症，请立即前往医院急诊科就医");
                    }
                    
                    aiResponse = doctorRecommendation.toString();
                    responseData.put("context", "COMPLETE");
                    responseData.put("type", "doctor_recommendation");
                    responseData.put("doctors", allDoctors);
                } else {
                    aiResponse = "好的，如果之后需要医生推荐，随时告诉我。建议您尽快就医，祝您早日康复！";
                    responseData.put("context", "COMPLETE");
                }
            }
            else {
                // 完成问诊或其他情况，重新开始
                aiResponse = "您好，我是智能医疗助手。请详细描述您的症状，我来帮您分析和提供建议。";
                responseData.put("context", "");
            }
            
            responseData.put("message", ChatMessage.assistantMessage(aiResponse));
            return Result.success(responseData);
            
        } catch (Exception e) {
            log.error("AI聊天处理失败", e);
            return Result.error("聊天处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据症状推荐科室
     * @param request 包含症状描述的请求体
     * @return 推荐的科室信息
     */
    @PostMapping("/recommend-department")
    public Result<?> recommendDepartment(@RequestBody Map<String, String> request) {
        String symptoms = request.getOrDefault("symptoms", "");
        
        if (symptoms.trim().isEmpty()) {
            return Result.error("症状描述不能为空");
        }
        
        log.info("收到科室推荐请求，症状描述: {}", symptoms);
        
        try {
            // 调用智谱AI服务进行科室推荐
            String departmentRecommendation = zhipuAiService.recommendDepartment(symptoms);
            
            // 返回推荐结果
            return Result.success(ChatMessage.assistantMessage(departmentRecommendation));
        } catch (Exception e) {
            log.error("科室推荐失败", e);
            return Result.error("科室推荐失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据症状推荐医生
     * @param request 包含症状描述的请求体
     * @return 推荐的医生列表
     */
    @PostMapping("/recommend-doctors")
    public Result<?> recommendDoctors(@RequestBody Map<String, Object> request) {
        String symptoms = (String) request.getOrDefault("symptoms", "");
        Integer limit = request.containsKey("limit") ? 
                       Integer.valueOf(request.get("limit").toString()) : 3;
        
        if (symptoms.trim().isEmpty()) {
            return Result.error("症状描述不能为空");
        }
        
        log.info("收到医生推荐请求，症状描述: {}, limit: {}", symptoms, limit);
        
        try {
            // 第一步：通过智谱AI获取科室推荐
            String departmentRecommendation = zhipuAiService.recommendDepartment(symptoms);
            
            // 第二步：解析推荐的科室
            String recommendedDepartment = extractDepartment(departmentRecommendation);
            
            if (recommendedDepartment == null || recommendedDepartment.isEmpty()) {
                return Result.error("无法识别合适的科室");
            }
            
            // 第三步：调用辅助方法获取医生推荐
            return recommendDoctors(symptoms, recommendedDepartment, limit);
        } catch (Exception e) {
            log.error("医生推荐失败", e);
            return Result.error("医生推荐失败: " + e.getMessage());
        }
    }
    
    /**
     * 推荐医生的辅助方法
     */
    private Result<?> recommendDoctors(String symptoms, String departmentName, int limit) {
        try {
            // 根据科室查询推荐医生
            List<Map<String, Object>> doctors = doctorService.findDoctorsByDepartment(departmentName, limit);
            
            if (doctors.isEmpty()) {
                log.warn("未找到科室[{}]的医生", departmentName);
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("message", ChatMessage.assistantMessage(
                        "根据您的症状，建议您去" + departmentName + "就诊，但目前系统中没有该科室的医生信息。建议您直接联系医院咨询。"));
                responseData.put("type", "doctor_recommendation");
                responseData.put("doctors", new ArrayList<>());
                return Result.success(responseData);
            }
            
            // 调用AI生成带有专业解释的医生推荐
            String aiRecommendation = zhipuAiService.recommendDoctorsWithExplanation(
                    symptoms, departmentName, doctors);
            
            // 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("type", "doctor_recommendation");
            responseData.put("message", ChatMessage.assistantMessage(aiRecommendation));
            responseData.put("doctors", doctors);
            responseData.put("department", departmentName);
            
            return Result.success(responseData);
        } catch (Exception e) {
            log.error("医生推荐失败", e);
            return Result.error("医生推荐失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查消息是否包含症状关键词
     */
    private boolean containsSymptomKeywords(String message) {
        String[] keywords = {
            // 常见症状
            "疼", "痛", "不舒服", "症状", "难受", "头晕", "发热", "感冒", "发烧", 
            "咳嗽", "喘", "腹泻", "恶心", "呕吐", "过敏", "出疹", "失眠",
            // 身体部位
            "头", "胸", "腹", "背", "腰", "脖子", "手", "脚", "关节", "肚子",
            // 症状描述
            "肿", "痒", "麻", "胀", "酸", "晕", "困", "累", "虚弱", "乏力",
            "心慌", "心悸", "气短", "胸闷", "头重", "头昏", "耳鸣", "视力模糊",
            // 时间描述
            "总是", "经常", "反复", "持续", "突然", "一直", "最近",
            // 程度描述
            "严重", "轻微", "剧烈", "强烈", "有点", "特别", "非常"
        };
        
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查是否需要医生推荐
     */
    private boolean needsDoctorRecommendation(String message) {
        String[] keywords = {"推荐医生", "找医生", "看哪个医生", "看什么医生", "哪位医生", "医生推荐", 
                           "推荐大夫", "推荐医师", "好的医生", "专业医生"};
        
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 从AI回复中提取推荐科室
     */
    private String extractDepartment(String aiResponse) {
        // 使用正则表达式提取科室名称
        // 格式：推荐科室：XXX科室 或 推荐科室：XXX
        Pattern pattern = Pattern.compile("推荐科室：([^\\n]+)");
        Matcher matcher = pattern.matcher(aiResponse);
        
        if (matcher.find()) {
            String department = matcher.group(1).trim();
            // 移除末尾的标点符号
            department = department.replaceAll("[，。,.、]$", "");
            
            // 处理"或"的情况 - 取第一个科室
            if (department.contains("或")) {
                log.info("检测到多科室推荐: {}", department);
                department = department.split("或")[0].trim();
                log.info("提取第一个科室: {}", department);
            }
            
            // 清理科室名称，移除"科室"后缀
            department = department.replaceAll("科室$", "科");
            
            // 根据数据库中的科室进行简单映射
            if (department.contains("呼吸")) {
                department = "呼吸内科";
            } else if (department.contains("感染")) {
                department = "感染科";
            }
            
            log.info("最终提取的科室名称: {}", department);
            return department;
        }
        
        return null;
    }
    
    /**
     * 从AI回复中提取推荐理由
     */
    private String extractReason(String aiResponse) {
        // 查找推荐原因
        Pattern pattern = Pattern.compile("(?:原因|理由|分析|考虑)[：:](.*?)(?=\\n|$)");
        Matcher matcher = pattern.matcher(aiResponse);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        // 如果没有找到明确的原因说明，返回整个分析内容
        // 移除科室推荐部分
        String reason = aiResponse.replaceAll("(?s)建议.*?科室.*?[：:].*?\\n", "")
                                .replaceAll("(?s)推荐.*?科室.*?[：:].*?\\n", "")
                                .trim();
        
        return reason.isEmpty() ? "基于症状的综合分析" : reason;
    }
    
    /**
     * 从AI回复中提取推荐科室列表
     */
    private List<String> extractDepartments(String aiResponse) {
        List<String> departments = new ArrayList<>();
        
        // 1. 首先尝试匹配"建议就诊科室："后的内容
        Pattern pattern = Pattern.compile("建议(?:就诊|到|看|选择)?(?:的)?科室[：:](.*?)(?=\\n|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(aiResponse);
        
        if (matcher.find()) {
            String departmentStr = matcher.group(1).trim();
            // 按分隔符分割科室名称
            String[] depts = departmentStr.split("[、，,。]");
            for (String dept : depts) {
                String cleanDept = cleanDepartmentName(dept.trim());
                if (!cleanDept.isEmpty()) {
                    departments.add(cleanDept);
                }
            }
        }
        
        // 2. 如果上面没找到，再尝试其他模式
        if (departments.isEmpty()) {
            // 尝试匹配"xx科"的模式
            Pattern deptPattern = Pattern.compile("([^，。、\\s]+科)");
            Matcher deptMatcher = deptPattern.matcher(aiResponse);
            while (deptMatcher.find()) {
                String dept = cleanDepartmentName(deptMatcher.group(1));
                if (!dept.isEmpty() && !departments.contains(dept)) {
                    departments.add(dept);
                }
            }
        }
        
        // 3. 过滤掉大科室
        departments.removeIf(dept -> dept.equals("内科") || dept.equals("外科") || 
                                   dept.equals("儿科") || dept.equals("妇科"));
        
        log.info("从AI回复中提取的科室列表: {}", departments);
        return departments;
    }
    
    private String cleanDepartmentName(String department) {
        if (department == null || department.isEmpty()) {
            return "";
        }
        
        // 1. 基础清理
        department = department.trim()
                             .replaceAll("[（(][^）)]*[）)]", "") // 移除括号内容
                             .replaceAll("^(去|到|选择|建议|推荐)", "") // 移除前缀动词
                             .replaceAll("^[-•*⚫️]\\s*", "") // 移除列表符号
                             .trim();
        
        // 2. 标准化科室名称
        Map<String, String> standardNames = new HashMap<>();
        standardNames.put("呼吸", "呼吸内科");
        standardNames.put("消化", "消化内科");
        standardNames.put("心血管", "心血管内科");
        standardNames.put("神经", "神经内科");
        standardNames.put("内分泌", "内分泌科");
        standardNames.put("皮科", "皮肤科");
        standardNames.put("五官科", "耳鼻喉科");
        
        // 3. 检查是否需要标准化
        for (Map.Entry<String, String> entry : standardNames.entrySet()) {
            if (department.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // 4. 不要自动添加"科"后缀，保持原样
        return department;
    }
    
    private List<String> getCommonDepartments() {
        try {
            // 从数据库获取所有科室
            List<String> departments = doctorMapper.getAllDepartmentNames();
            if (departments == null || departments.isEmpty()) {
                log.warn("未从数据库获取到科室列表，使用默认科室");
                // 使用一个小的默认列表作为备份
                return Arrays.asList("呼吸内科", "消化内科", "神经内科", "心血管内科");
            }
            return departments;
        } catch (Exception e) {
            log.error("获取科室列表失败", e);
            // 发生错误时返回一个小的默认列表
            return Arrays.asList("呼吸内科", "消化内科", "神经内科", "心血管内科");
        }
    }
} 