package com.oppo.cloud.diagnosis.domain.dto;


import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.Serializable;

/**
 * 操作消息提醒
 *
 */
@Getter
@Setter
public class OResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer status;
    private String msg;
    private T data;
    private String[] stack;

    public OResult() {
    }


    public static <T> OResult<T> success() {
        OResult<T> result = new OResult<>();
        result.setMsg("SUCCESS");
        result.setStatus(HttpStatus.SUCCESS);
        result.setData(null);
        return result;
    }

    public static <T> OResult<T> fail() {
        OResult<T> result = new OResult<>();
        result.setMsg("请求处理出错");
        return result;
    }

    public static <T> OResult<T> success(T t) {
        OResult<T> result = new OResult<>();
        result.setMsg("SUCCESS");
        result.setStatus(HttpStatus.SUCCESS);
        result.setData(t);
        return result;
    }

    public static <T> OResult<T> success(T t, String msg) {
        OResult<T> result = new OResult<>();
        result.setMsg(msg);
        result.setData(t);
        result.setStatus(HttpStatus.SUCCESS);
        return result;
    }

    public static <T> OResult<T> paramException(String msg) {
        OResult<T> result = new OResult<>();
        result.setStatus(HttpStatus.BAD_REQUEST);
        result.setMsg(msg);
        return result;
    }

    public static <T> OResult<T> fail(String msg) {
        OResult<T> result = new OResult<>();
        result.setMsg(msg);
        return result;
    }

    public static <T> OResult<T> fail(int status, String msg) {
        OResult<T> result = new OResult<>();
        result.setStatus(status);
        result.setMsg(msg);
        return result;
    }

    public static <T> OResult<T> fail(Throwable e) {
        OResult<T> result = new OResult<>();
        result.setMsg(StringUtils.defaultString(e.getMessage(), e.toString()));
        result.setStack(ExceptionUtils.getStackFrames(e));
        return result;
    }

}
