package com.pdm.common;

public final class ErrorCode {

    public static final int SUCCESS = 0;
    public static final int BAD_PARAM = 40001;
    public static final int UNAUTHORIZED = 40101;
    public static final int FORBIDDEN = 40301;
    public static final int RECHECK_FAILED = 40302;
    public static final int NOT_FOUND = 40401;
    public static final int CONFLICT = 40901;
    public static final int LOCKED = 42301;
    public static final int SERVER_ERROR = 50001;

    private ErrorCode() {
    }
}