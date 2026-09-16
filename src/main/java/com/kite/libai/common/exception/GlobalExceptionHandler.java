package com.kite.libai.common.exception;

import com.kite.libai.common.core.domain.R;
import com.kite.libai.common.enums.ResultCode;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理器。
 *
 * <p>统一把各类异常转换为 {@link R} 结构,并保持 HTTP 状态码与业务 code 一致,
 * 避免出现"HTTP 200 但实际失败"的情况,便于监控与网关按状态码统计。
 *
 * @author kite
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常:属于预期内的流程分支,只记录简要信息,不打印堆栈。
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<R<Void>> handleServiceException(ServiceException ex) {
        log.warn("业务处理失败: {}", ex.getMessage());
        HttpStatus status = HttpStatus.resolve(ex.getCode());
        return ResponseEntity
                .status(status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status)
                .body(R.fail(ex.getCode(), ex.getMessage()));
    }

    /**
     * 请求体参数校验失败(@RequestBody + @Valid)。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return badRequest(detail);
    }

    /**
     * 表单/查询参数绑定校验失败。
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<R<Void>> handleBindException(BindException ex) {
        String detail = ex.getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return badRequest(detail);
    }

    /**
     * 方法级参数校验失败。
     *
     * <p>Spring 5.x 没有内置的控制器方法参数校验,{@code @Validated} 走的是 AOP 代理,
     * 抛出的是 {@link ConstraintViolationException},默认会变成 500,这里显式转成 400。
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<R<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        String detail = ex.getConstraintViolations().stream()
                .map(this::formatViolation)
                .sorted()
                .collect(Collectors.joining("; "));
        return badRequest(detail);
    }

    /**
     * 缺少必填请求参数。
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<R<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        return badRequest("缺少必填参数: " + ex.getParameterName());
    }

    /**
     * 参数类型不匹配,例如把非数字传给 Long 类型的路径变量。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<R<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return badRequest("参数类型不正确: " + ex.getName());
    }

    /**
     * 请求方法不支持。
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<R<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(R.fail(HttpStatus.METHOD_NOT_ALLOWED.value(), "不支持的请求方法: " + ex.getMethod()));
    }

    /**
     * 兜底处理:未预期的异常必须打印完整堆栈,同时对外隐藏内部细节。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleException(Exception ex) {
        log.error("系统异常", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(R.fail(ResultCode.SYSTEM_ERROR));
    }

    private ResponseEntity<R<Void>> badRequest(String detail) {
        String msg = (detail == null || detail.isEmpty()) ? ResultCode.PARAM_INVALID.getMsg() : detail;
        return ResponseEntity.badRequest().body(R.fail(ResultCode.PARAM_INVALID, msg));
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + " " + error.getDefaultMessage();
    }

    private String formatViolation(ConstraintViolation<?> violation) {
        return violation.getPropertyPath() + " " + violation.getMessage();
    }
}
