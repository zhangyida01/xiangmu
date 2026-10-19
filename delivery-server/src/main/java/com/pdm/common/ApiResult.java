package com.pdm.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiResult<T> implements Serializable {

    private int code;
    private String msg;
    private T data;

    public static <T> ApiResult<T> ok() {
        return build(ErrorCode.SUCCESS, "ok", null);
    }

    public static <T> ApiResult<T> ok(T data) {
        return build(ErrorCode.SUCCESS, "ok", data);
    }

    public static <T> ApiResult<T> ok(String msg, T data) {
        return build(ErrorCode.SUCCESS, msg, data);
    }

    public static <T> ApiResult<T> fail(int code, String msg) {
        return build(code, msg, null);
    }

    private static <T> ApiResult<T> build(int code, String msg, T data) {
        ApiResult<T> r = new ApiResult<>();
        r.code = code;
        r.msg = msg;
        r.data = data;
        return r;
    }
}