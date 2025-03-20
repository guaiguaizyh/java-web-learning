package com.itheima.pojo;
/*实体类*/
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dept {
    private Integer id;
    private String name;
    private LocalDateTime createTime;/*实体类使用规范驼峰名*/
    private LocalDateTime updateTime;
}