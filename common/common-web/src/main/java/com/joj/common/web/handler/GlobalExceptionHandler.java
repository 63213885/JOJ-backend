package com.joj.common.web.handler;

import com.joj.common.core.exception.BusinessException;
import com.joj.common.core.exception.ErrorCode;
import com.joj.common.core.model.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

/**
 * @author jzz
 * @github <a href="https://github.com/63213885">63213885</a>
 * @createtime 2026/4/8 22:28
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验失败（@Valid）统一返回：HTTP 400。
     * 仅取首个字段错误的信息作为提示。
     *
     * @param ex Spring 的方法参数校验异常。
     * @return 响应体：code/message。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidation(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException", ex);
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse(ErrorCode.PARAMS_ERROR.getMessage());
        return Result.error(ErrorCode.PARAMS_ERROR, message);
    }

    /**
     * 约束校验失败（如 @Validated 参数）统一返回：HTTP 400。
     *
     * @param ex 参数约束异常。
     * @return 响应体：code/message。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraintViolation(ConstraintViolationException ex) {
        log.error("ConstraintViolationException", ex);
        return Result.error(ErrorCode.PARAMS_ERROR, ex.getMessage());
    }

    /**
     * 处理运行时异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return Result.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}
