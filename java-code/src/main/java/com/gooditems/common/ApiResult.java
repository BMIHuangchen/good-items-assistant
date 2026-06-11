package com.gooditems.common;

public record ApiResult<T>(int code, String message, T data, String requestId) {
    public static <T> ApiResult<T> ok(T data, String requestId) {
        return new ApiResult<>(200, "success", data, requestId);
    }

    public static ApiResult<Void> ok(String requestId) {
        return new ApiResult<>(200, "success", null, requestId);
    }

    public static <T> ApiResult<T> error(int code, String message, String requestId) {
        return new ApiResult<>(code, message, null, requestId);
    }
}
