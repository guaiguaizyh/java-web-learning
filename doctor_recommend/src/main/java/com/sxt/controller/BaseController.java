package com.sxt.controller;

import com.sxt.pojo.Result;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 基础控制器，提供通用功能
 */
public abstract class BaseController {
    
    /**
     * 批量删除
     * @param ids ID列表
     * @return 删除结果
     */
    // 已废弃：批量删除方法，当前未被任何Controller继承或使用，故移除
    // @DeleteMapping("/batch")
    // @ApiOperation("批量删除")
    // public Result<Void> batchDelete(@RequestBody List<Integer> ids) {
    //     try {
    //         if (ids == null || ids.isEmpty()) {
    //             return Result.error("请选择要删除的记录");
    //         }
    //         boolean success = doBatchDelete(ids);
    //         return success ? Result.success(null) : Result.error("批量删除失败");
    //     } catch (Exception e) {
    //         return Result.error("批量删除失败：" + e.getMessage());
    //     }
    // }

    /**
     * 执行批量删除操作
     * @param ids ID列表
     * @return 删除结果
     */
    // protected abstract boolean doBatchDelete(List<Integer> ids);
} 