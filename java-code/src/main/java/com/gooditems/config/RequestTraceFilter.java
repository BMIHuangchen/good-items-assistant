package com.gooditems.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestTraceFilter extends OncePerRequestFilter {
    public static final String REQUEST_ID = "requestId";
    private static final Logger log = LoggerFactory.getLogger(RequestTraceFilter.class);
    private final AppProperties properties;

    public RequestTraceFilter(AppProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = firstNonBlank(request.getHeader("X-Request-Id"), UUID.randomUUID().toString());
        long started = System.currentTimeMillis();
        request.setAttribute(REQUEST_ID, requestId);
        response.setHeader("X-Request-Id", requestId);
        MDC.put(REQUEST_ID, requestId);
        try {
            chain.doFilter(request, response);
        } finally {
            long cost = System.currentTimeMillis() - started;
            String level = cost >= properties.getDiagnostics().getSlowRequestMs() ? "slow" : "ok";
            log.info("request={} method={} path={} status={} costMs={} remote={} ua={}",
                    level, request.getMethod(), request.getRequestURI(), response.getStatus(), cost,
                    request.getRemoteAddr(), request.getHeader("User-Agent"));
            MDC.remove(REQUEST_ID);
        }
    }

    private String firstNonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
