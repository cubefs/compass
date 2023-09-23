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

package com.oppo.cloud.portal.config;

import com.oppo.cloud.common.api.CommonStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionConfig {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public CommonStatus exceptionHandler(Exception e) {
        log.error("GlobalException: ", e);
        return CommonStatus.failed(e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public CommonStatus validationHandler(ConstraintViolationException ex) {
        StringBuilder builder = new StringBuilder();
        for (ConstraintViolation<?> s : ex.getConstraintViolations()) {
            builder.append(s.getInvalidValue() + ": " + s.getMessage()).append(";");
        }
        return CommonStatus.failed(builder.toString());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public CommonStatus validationHandler1(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        return CommonStatus.failed(fieldError.getDefaultMessage());
    }

    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public CommonStatus parameterMissingExceptionHandler(MissingServletRequestParameterException e) {
        return CommonStatus.failed("parameter: " + e.getParameterName() + " can not be empty.");
    }

}
