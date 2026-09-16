package com.kite.libai.common.enums;

/**
 * 统一响应状态码。
 *
 * <p>取值沿用 HTTP 语义,业务细分错误统一归到 400 段,便于前端按 code 分支处理。
 *
 * @author kite
 */
public enum ResultCode {

    /** 操作成功 */
    SUCCESS(200, "操作成功"),

    /** 参数校验失败 */
    PARAM_INVALID(400, "请求参数不正确"),

    /** 未登录或凭证已过期 */
    UNAUTHORIZED(401, "登录状态已过期,请重新登录"),

    /** 无访问权限 */
    FORBIDDEN(403, "没有操作权限"),

    /** 资源不存在 */
    NOT_FOUND(404, "请求的资源不存在"),

    /** 业务处理失败 */
    BUSINESS_ERROR(500, "操作失败"),

    /** 系统内部错误 */
    SYSTEM_ERROR(500, "系统繁忙,请稍后重试");

    private final int code;

    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
