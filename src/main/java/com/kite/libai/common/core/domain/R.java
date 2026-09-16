package com.kite.libai.common.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kite.libai.common.enums.ResultCode;
import java.io.Serializable;

/**
 * 统一响应结果包装。
 *
 * @param <T> 业务数据类型
 * @author kite
 */
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码,200 表示成功 */
    private int code;

    /** 提示信息 */
    private String msg;

    /** 业务数据 */
    private T data;

    public R() {
    }

    public R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(ResultCode.BUSINESS_ERROR.getCode(), msg, null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null);
    }

    public static <T> R<T> fail(ResultCode resultCode) {
        return new R<>(resultCode.getCode(), resultCode.getMsg(), null);
    }

    public static <T> R<T> fail(ResultCode resultCode, String msg) {
        return new R<>(resultCode.getCode(), msg, null);
    }

    /**
     * 按影响行数返回,常用于新增、修改、删除等写操作。
     *
     * @param rows 受影响行数
     */
    public static R<Void> toResult(int rows) {
        return rows > 0 ? R.ok() : R.fail("操作失败,数据未发生变更");
    }

    /** 仅供服务端判断使用,不参与 JSON 序列化,避免响应体多出 success 字段 */
    @JsonIgnore
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode() == this.code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
