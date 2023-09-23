/*
 * Copyright 2023 OPPO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oppo.cloud.common.api;

import lombok.Data;

/**
 * 通用返回对象
 */
@Data
public class CommonStatus<T> {

    /**
     * 状态码
     */
    private int code;
    /**
     * 提示信息
     */
    private String msg;
    /**
     * 数据封装
     */
    private T data;

    protected CommonStatus(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> CommonStatus<T> success(T data) {
        return new CommonStatus<T>(StatusCode.SUCCESS.getCode(), StatusCode.SUCCESS.getMsg(), data);
    }

    public static <T> CommonStatus<T> success(T data, String msg) {
        return new CommonStatus<T>(StatusCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> CommonStatus<T> failed(IErrorCode errorCode) {
        return new CommonStatus<T>(errorCode.getCode(), errorCode.getMsg(), null);
    }

    public static <T> CommonStatus<T> failed(IErrorCode errorCode, String msg) {
        return new CommonStatus<T>(errorCode.getCode(), msg, null);
    }

    public static <T> CommonStatus<T> failed(String msg) {
        return new CommonStatus<T>(StatusCode.FAILED.getCode(), msg, null);
    }

    public static <T> CommonStatus<T> validateFailed(String message) {
        return new CommonStatus<T>(StatusCode.VALIDATE_FAILED.getCode(), message, null);
    }

    public static <T> CommonStatus<T> unauthorized(String message) {
        return new CommonStatus<T>(StatusCode.UNAUTHORIZED.getCode(), message, null);
    }

    @Override
    public String toString() {
        return String.format("{\"code\": %d, \"message\": \"%s\", \"data\":%s}", this.code, this.msg, this.data);
    }
}
