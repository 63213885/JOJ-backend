package com.joj.common.exception;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/8 17:23
 */

import lombok.Getter;

/**
 * 自定义异常类
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

}
