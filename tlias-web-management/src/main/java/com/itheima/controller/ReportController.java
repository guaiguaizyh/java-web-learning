package com.itheima.controller;

import com.itheima.pojo.ClazzOption;
import com.itheima.pojo.JobOption;
import com.itheima.pojo.Result;
import com.itheima.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping("/report")
@RestController
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 统计各个职位的员工人数
     */
    @GetMapping("/empJobData")
    public Result getEmpJobData(){
        log.info("统计各个职位的员工人数");
        JobOption jobOption = reportService.getEmpJobData();
        return Result.success(jobOption);
    }

    //统计员工性别信息
    @GetMapping("/empGenderData")
    public Result getEmpGenderData(){
        log.info("统计员工性别信息");
        List<Map<String, Object>> data = reportService.getEmpGenderData();
        return Result.success(data);
    }
    //统计员工学历信息
    @GetMapping("/studentDegreeData")
    public Result getStudentDegreeData(){
        log.info("统计学员学历信息");
        List<Map<String, Object>> data = reportService.getStudentDegreeData();
        return Result.success(data);
    }

    //统计班级人数
    @GetMapping("/studentCountData")
    public Result getStudentCountData(){
        log.info("统计班级人数");
        ClazzOption clazzOption= reportService.getStudentCountData();
        return Result.success(clazzOption);
    }
}
