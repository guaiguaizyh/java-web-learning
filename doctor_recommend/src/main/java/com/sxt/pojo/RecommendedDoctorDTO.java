package com.sxt.pojo; // 或者你倾向的 DTO 包名，如果不想放在 pojo 下

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 用于症状推荐结果中展示的医生信息 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedDoctorDTO {

    private Integer doctorId; // Doctor ID (数据库是 INT)
    private String name;    // 医生姓名
    private String gender;  // 性别
    private String departmentName; // 科室名称 (来自 departments.department_name)
    private Integer positionsId; // 职称ID (来自 doctors.positions_id)
    private String positionsName; // 职称名称 (来自 positions表)
    private Integer workYears; // 工作年限 (来自 doctors.work_years)
    private Float averageRating; // 平均评分 (来自 doctors.average_rating)
    private Integer ratingCount; // 评分次数 (来自 doctors.rating_count)
    private String avatarUrl; // 头像 URL (来自 doctors.avatar_url)

    // 可以根据需要添加其他字段，例如：
    private String expertiseList; // 医生专长列表 (需要额外查询)
    private String expertiseListStr; // 医生专长字符串，用于详情页面展示
    private Float matchScore; // 匹配度分数
    private String recommendReason; // 推荐原因

    // 添加matchLabel字段
    private String matchLabel;

    // 添加getter和setter方法
    public String getMatchLabel() {
        return matchLabel;
    }

    public void setMatchLabel(String matchLabel) {
        this.matchLabel = matchLabel;
    }
}