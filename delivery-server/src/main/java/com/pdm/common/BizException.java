package com.pdm.common;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String msg) {
        this(ErrorCode.CONFLICT, msg);
    }

    public BizException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public static BizException notFound(String what) {
        return new BizException(ErrorCode.NOT_FOUND, what + "不存在");
    }

    public static BizException forbidden(String msg) {
        return new BizException(ErrorCode.FORBIDDEN, msg);
    }

    public static BizException badParam(String msg) {
        return new BizException(ErrorCode.BAD_PARAM, msg);
    }

    public static BizException conflict(String msg) {
        return new BizException(ErrorCode.CONFLICT, msg);
    }
}