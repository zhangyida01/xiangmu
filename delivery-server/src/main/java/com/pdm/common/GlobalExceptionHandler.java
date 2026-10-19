package com.pdm.common;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ApiResult<Void> handleBiz(BizException e, HttpServletRequest req) {
        log.warn("业务异常 {} {} -> {} {}", req.getMethod(), req.getRequestURI(), e.getCode(), e.getMessage());
        return ApiResult.fail(e.getCode(), e.getMessage());
    }

    /** 唯一约束冲突：数据库抛出的 DuplicateKeyException 不把底层 SQL 细节透给前端（详情看日志） */
    @ExceptionHandler(DuplicateKeyException.class)
    public ApiResult<Void> handleDuplicateKey(DuplicateKeyException e, HttpServletRequest req) {
        log.warn("唯一约束冲突 {} {}", req.getMethod(), req.getRequestURI(), e);
        return ApiResult.fail(ErrorCode.CONFLICT, "数据已存在：编号/账号等唯一字段重复，请更换后重试");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ApiResult<Void> handleValid(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " " + defaultMessage(f))
                .collect(Collectors.joining("; "));
        return ApiResult.fail(ErrorCode.BAD_PARAM, msg.isEmpty() ? "参数校验失败" : msg);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResult<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return ApiResult.fail(ErrorCode.BAD_PARAM, "缺少参数：" + e.getParameterName());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResult<Void> handleUploadSize(MaxUploadSizeExceededException e) {
        return ApiResult.fail(ErrorCode.BAD_PARAM, "上传文件超过大小限制");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResult<Void> handleMethod(HttpRequestMethodNotSupportedException e) {
        return ApiResult.fail(ErrorCode.BAD_PARAM, "请求方法不支持：" + e.getMethod());
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleOther(Exception e, HttpServletRequest req) {
        log.error("系统异常 {} {}", req.getMethod(), req.getRequestURI(), e);
        return ApiResult.fail(ErrorCode.SERVER_ERROR, "服务器内部错误：" + e.getMessage());
    }

    private String defaultMessage(FieldError f) {
        return f.getDefaultMessage() == null ? "格式不正确" : f.getDefaultMessage();
    }
}