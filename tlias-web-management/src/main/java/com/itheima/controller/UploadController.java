package com.itheima.controller;

import com.itheima.pojo.Result;
import com.itheima.utils.AliyunOSSOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
public class UploadController {

    @Autowired
    private AliyunOSSOperator aliyunOSSOperator;

//    public UploadController(AliyunOSSOperator aliyunOSSOperator) {
//        this.aliyunOSSOperator = aliyunOSSOperator;
//    }

    /**
     * 上传文件 - 参数名file
     */
    //存入本地磁盘
//    @PostMapping("/upload")
//    public Result upload(String username, Integer age , MultipartFile file) throws Exception {
//        log.info("上传文件：{}, {}, {}", username, age, file);
//        //获取原始文件名
//        String originalFilename = file.getOriginalFilename();
//        //新的文件名,与原始的文件名【后缀】保持一致，【找到最后一个.的位置，截取后缀】
//        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
//        String newFileName = System.currentTimeMillis() + extension;
//        //保存文件 将接受的【新】文件转到磁盘中
//        file.transferTo(new File("E:/web/Qianduan/images/" +newFileName));
//        //如何保证文件名不重复，使用UUID生成不重复的ID
//        return Result.success();
//    }

    //阿里云OSS
    @PostMapping("/upload")
    public Result upload(MultipartFile file) throws Exception {
        log.info("上传文件：{}", file);
        if (!file.isEmpty()) {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extName = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + extName;
            // 上传文件
            String url = aliyunOSSOperator.upload(file.getBytes(), uniqueFileName);
            return Result.success(url);
        }
        return Result.error("上传失败");
    }
}