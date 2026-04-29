package com.joj.common.model.result;

import com.joj.common.exception.ErrorCode;
import lombok.Data;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/8 22:21
 */

/**
 * 通用返回类
 *
 * @param <T>
 */
@Data
public class Result<T> {

    private Integer code; //编码：1成功，0和其它数字为失败
    private String msg; //错误信息
    private T data; //数据

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = 0;
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 0;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<T>();
        result.msg = msg;
        result.code = 50000;
        return result;
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> result = new Result<T>();
        result.code = code;
        result.msg = msg;
        return result;
    }

    public static <T> Result<T> error(ErrorCode errorCode, String msg) {
        Result<T> result = new Result<T>();
        result.code = errorCode.getCode();
        result.msg = msg;
        return result;
    }

}
