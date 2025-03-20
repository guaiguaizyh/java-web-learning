package com.itheima.service;

import com.itheima.pojo.ClazzOption;
import com.itheima.pojo.JobOption;

import java.util.List;
import java.util.Map;

public interface ReportService {
    /**
     * 统计各个职位的员工人数
     * @return
     */
    JobOption getEmpJobData();
    //性别信息
    List<Map<String, Object>> getEmpGenderData();
    //学员学历信息
    List<Map<String, Object>> getStudentDegreeData();
    //统计班级人数//根据接口文档，需要创建一个类用于存班级名称和班级对应的人数
    ClazzOption getStudentCountData();
}