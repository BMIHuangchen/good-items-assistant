package com.gooditems.exception;

import com.gooditems.common.ApiResult;
import com.gooditems.config.RequestTraceFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ApiResult<Void> handleApiException(ApiException e, HttpServletRequest request) {
        String requestId = requestId(request);
        log.warn("api error requestId={} code={} message={}", requestId, e.getCode(), e.getMessage());
        return ApiResult.error(e.getCode(), e.getMessage(), requestId);
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleException(Exception e, HttpServletRequest request) {
        String requestId = requestId(request);
        log.error("server error requestId={}", requestId, e);
        return ApiResult.error(500, "服务暂时不可用，请稍后再试。排查编号：" + requestId, requestId);
    }

    private String requestId(HttpServletRequest request) {
        Object value = request.getAttribute(RequestTraceFilter.REQUEST_ID);
        return value == null ? "" : value.toString();
    }
}
