package com.blackfire.aicloud.provider.exception;

import com.blackfire.aicloud.common.exception.BusinessException;
import com.blackfire.aicloud.provider.domain.resp.GatewayResp;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * @author zhaominglei
 * @version 1.0
 * @date 2020年12月22日
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxSize;
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setAllowedFields("selfResearchCost", "selfRealResearchCost", "accessUrl");
        binder.setDisallowedFields("deleted", "createUser", "updateUser", "createTime", "updateTime");
    }

    @ExceptionHandler(BusinessException.class)
    public GatewayResp uploadException(BusinessException businessException) {
        return GatewayResp.error(businessException.getErrorCode(), businessException.getErrorMsg());
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public GatewayResp handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                       HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod());
        return GatewayResp.error("请求方式不支持。");
    }

    /**
     * 参数设定异常
     */
    @ExceptionHandler(ServletRequestBindingException.class)
    public GatewayResp handleRequestBindingException(ServletRequestBindingException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生系统异常.", requestURI, e);
        return GatewayResp.error(e.getMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(RuntimeException.class)
    public GatewayResp handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生未知异常.", requestURI, e);
        return GatewayResp.error();
    }

    /**
     * 资源找不到
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}', 资源未找到.", requestURI);
        response.sendError(404, "No resource found");
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public GatewayResp handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生系统异常.", requestURI, e);
        return GatewayResp.error();
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public GatewayResp handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);
        String errMsg = e.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(";"));
        return GatewayResp.error(errMsg);
    }

    @ExceptionHandler(FileSizeLimitExceededException.class)
    public GatewayResp fileSizeException(FileSizeLimitExceededException exception) {
        return GatewayResp.error("文件大小超过限制" + maxSize + "，请重新上传。");
    }
}
