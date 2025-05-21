package com.sxt.response;

import lombok.Data;

@Data
public class ApiResult<T> {
    /**
     * 状态码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    private ApiResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResult<T> success() {
        return new ApiResult<>(200, "操作成功", null);
    }

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(200, "操作成功", data);
    }

    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>(200, message, data);
    }

    public static <T> ApiResult<T> error(String message) {
        return new ApiResult<>(500, message, null);
    }

    public static <T> ApiResult<T> error(Integer code, String message) {
        return new ApiResult<>(code, message, null);
    }
} 