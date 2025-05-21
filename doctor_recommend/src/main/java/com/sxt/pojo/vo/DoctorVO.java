package com.sxt.pojo.vo;

import lombok.Data;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import com.sxt.pojo.Expertise;
import com.sxt.pojo.DoctorExpertise;

@Data
public class DoctorVO {
    private Integer doctorId;
    private String name;
    private String gender;
    private Integer age;
    private Integer positionsId;
    private String positionsName; // 职称名称（显示用）
    private Integer departmentId;
    private String departmentName;
    private String avatarUrl;
    private Float averageRating;
    private Integer ratingCount;
    private Integer workYears; // 工作年龄/工作时长
    private List<Expertise> expertiseList;
    // 专长名称字符串，以逗号分隔
    private String expertiseListStr;
    // 专长详情（包含熟练度）
    private String expertiseDetails;
    
    // 专长熟练度映射 {专长ID: 熟练度}
    private Map<Integer, Float> expertiseProficiencyMap;
    
    // 专长列表（包含熟练度）
    private List<DoctorExpertise> doctorExpertises;
    
    /**
     * 获取专长熟练度映射
     * @return 专长ID到熟练度的映射
     */
    public Map<Integer, Float> getExpertiseProficiencyMap() {
        if (this.expertiseProficiencyMap != null) {
            return this.expertiseProficiencyMap;
        }
        
        this.expertiseProficiencyMap = new HashMap<>();
        if (expertiseDetails != null && !expertiseDetails.isEmpty()) {
            String[] expertiseItems = expertiseDetails.split(";");
            for (String item : expertiseItems) {
                String[] parts = item.split(":");
                if (parts.length >= 3) {
                    try {
                        Integer expertiseId = Integer.parseInt(parts[0]);
                        Float proficiency = Float.parseFloat(parts[2]);
                        this.expertiseProficiencyMap.put(expertiseId, proficiency);
                    } catch (NumberFormatException e) {
                        // 忽略无效数据
                    }
                }
            }
        }
        
        return this.expertiseProficiencyMap;
    }
    
    /**
     * 获取医生专长列表（包含熟练度和专长名称）
     * @return 医生专长列表
     */
    public List<DoctorExpertise> getDoctorExpertises() {
        if (this.doctorExpertises != null) {
            return this.doctorExpertises;
        }
        
        this.doctorExpertises = new ArrayList<>();
        if (expertiseDetails != null && !expertiseDetails.isEmpty()) {
            String[] expertiseItems = expertiseDetails.split(";");
            for (String item : expertiseItems) {
                String[] parts = item.split(":");
                if (parts.length >= 3) {
                    try {
                        DoctorExpertise doctorExpertise = new DoctorExpertise();
                        doctorExpertise.setDoctorId(this.doctorId);
                        doctorExpertise.setExpertiseId(Integer.parseInt(parts[0]));
                        doctorExpertise.setExpertiseName(parts[1]);
                        doctorExpertise.setProficiency(Float.parseFloat(parts[2]));
                        this.doctorExpertises.add(doctorExpertise);
                    } catch (NumberFormatException e) {
                        // 忽略无效数据
                    }
                }
            }
        }
        
        return this.doctorExpertises;
    }
    
    /**
     * 获取指定专长的熟练度
     * @param expertiseId 专长ID
     * @return 熟练度，不存在时返回null
     */
    public Float getProficiencyByExpertiseId(Integer expertiseId) {
        return getExpertiseProficiencyMap().get(expertiseId);
    }
}
