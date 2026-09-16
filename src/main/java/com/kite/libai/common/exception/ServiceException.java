package com.kite.libai.common.exception;

import com.kite.libai.common.enums.ResultCode;

/**
 * 业务异常。
 *
 * <p>用于表达"请求本身合法,但不满足业务规则"的场景,例如部门名称重复、
 * 删除仍有下级的部门等。由全局异常处理器统一转换为响应体,不打印堆栈。
 *
 * @author kite
 */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 响应状态码 */
    private final int code;

    public ServiceException(String message) {
        super(message);
        this.code = ResultCode.BUSINESS_ERROR.getCode();
    }

    public ServiceException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    public ServiceException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public int getCode() {
        return code;
    }

    /**
     * 业务规则校验失败,返回 400。
     *
     * @param message 面向用户的提示信息
     */
    public static ServiceException badRequest(String message) {
        return new ServiceException(ResultCode.PARAM_INVALID, message);
    }

    /**
     * 数据不存在,返回 404。
     *
     * @param message 面向用户的提示信息
     */
    public static ServiceException notFound(String message) {
        return new ServiceException(ResultCode.NOT_FOUND, message);
    }
}
