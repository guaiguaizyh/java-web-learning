# 智谱AI医生推荐功能后端实现方案

## 控制器实现

需要在`ZhipuAiController`中添加医生推荐的接口方法：

```java
/**
 * 根据症状直接推荐医生
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
        String reason = extractReason(departmentRecommendation);
        
        if (recommendedDepartment == null || recommendedDepartment.isEmpty()) {
            return Result.error("无法识别合适的科室");
        }
        
        // 第三步：根据科室查询推荐医生
        List<Map<String, Object>> doctors = doctorService.findDoctorsByDepartment(recommendedDepartment, limit);
        
        // 第四步：构建响应数据
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("recommendedDepartment", recommendedDepartment);
        responseData.put("reason", reason);
        responseData.put("doctors", doctors);
        
        return Result.success(responseData);
    } catch (Exception e) {
        log.error("AI医生推荐失败", e);
        return Result.error("医生推荐失败: " + e.getMessage());
    }
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
        return department;
    }
    
    return null;
}

/**
 * 从AI回复中提取推荐理由
 */
private String extractReason(String aiResponse) {
    // 去除"推荐科室：XXX"这一行，剩下的内容就是理由
    String[] lines = aiResponse.split("\\n");
    StringBuilder reason = new StringBuilder();
    
    boolean isFirstLineProcessed = false;
    for (String line : lines) {
        if (!isFirstLineProcessed && line.contains("推荐科室：")) {
            isFirstLineProcessed = true;
            continue;
        }
        
        if (isFirstLineProcessed && !line.trim().isEmpty()) {
            reason.append(line.trim()).append(" ");
        }
    }
    
    return reason.toString().trim();
}
```

## 医生服务实现

在`DoctorService`中添加根据科室查询医生的方法：

```java
/**
 * 根据科室名称查询医生
 * @param department 科室名称
 * @param limit 返回结果限制
 * @return 医生列表
 */
List<Map<String, Object>> findDoctorsByDepartment(String department, int limit);
```

在`DoctorServiceImpl`中实现该方法：

```java
@Override
public List<Map<String, Object>> findDoctorsByDepartment(String department, int limit) {
    // 查询指定科室的医生
    List<Doctor> doctors = doctorMapper.selectDoctorsByDepartment(department, limit);
    
    // 转换为前端需要的格式
    return doctors.stream().map(doctor -> {
        Map<String, Object> doctorMap = new HashMap<>();
        doctorMap.put("id", doctor.getId());
        doctorMap.put("name", doctor.getName());
        doctorMap.put("title", doctor.getTitle());
        doctorMap.put("department", department);
        doctorMap.put("hospital", doctor.getHospital());
        doctorMap.put("specialty", doctor.getSpecialty());
        doctorMap.put("rating", doctor.getRating());
        doctorMap.put("reviewCount", doctor.getReviewCount());
        return doctorMap;
    }).collect(Collectors.toList());
}
```

## 数据库查询

在`DoctorMapper`中添加查询方法：

```java
/**
 * 根据科室名称查询医生，按评分降序排序
 * @param department 科室名称
 * @param limit 返回结果限制
 * @return 医生列表
 */
List<Doctor> selectDoctorsByDepartment(@Param("department") String department, @Param("limit") int limit);
```

对应的XML映射：

```xml
<select id="selectDoctorsByDepartment" resultType="com.sxt.pojo.Doctor">
    SELECT d.*
    FROM doctor d
    JOIN department dept ON d.department_id = dept.id
    WHERE dept.name LIKE CONCAT('%', #{department}, '%')
    ORDER BY d.rating DESC
    LIMIT #{limit}
</select>
```

## 实现备选方案

如果无法精确匹配科室名称，可以考虑以下备选方案：

1. **模糊匹配**：使用LIKE查询，匹配包含关键词的科室
2. **关键词映射**：建立常见症状描述与科室的映射表
3. **回退策略**：如果无法匹配科室，返回评分最高的医生

```java
// 在DoctorServiceImpl中添加回退方法
public List<Map<String, Object>> findTopRatedDoctors(int limit) {
    List<Doctor> doctors = doctorMapper.selectTopRatedDoctors(limit);
    
    // 转换为前端需要的格式（同上）
    return doctors.stream().map(doctor -> {
        // 转换逻辑...
    }).collect(Collectors.toList());
}

// 在DoctorMapper中添加查询方法
@Select("SELECT * FROM doctor ORDER BY rating DESC LIMIT #{limit}")
List<Doctor> selectTopRatedDoctors(int limit);
```

## 优化建议

1. **缓存常见症状-科室映射**：对于常见症状，可以建立缓存，减少AI调用
2. **智能匹配算法**：使用自然语言处理技术，实现症状与科室的更准确匹配
3. **个性化推荐**：结合用户历史就诊记录，提供更个性化的医生推荐
4. **分词优化**：对症状描述进行分词处理，提取关键症状，提高匹配精度 