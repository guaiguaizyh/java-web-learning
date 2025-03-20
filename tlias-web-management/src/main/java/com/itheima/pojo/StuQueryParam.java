package com.itheima.pojo;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class StuQueryParam {
        private Integer page = 1; //页码
        private Integer pageSize = 2; //每页展示记录数
        private String name; //姓名
        private Integer degree;//学历
        private Integer clazzId;//班级
}
