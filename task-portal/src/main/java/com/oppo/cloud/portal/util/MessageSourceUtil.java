package com.oppo.cloud.portal.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageSourceUtil {

    private static MessageSource messageSource;

    public MessageSourceUtil(MessageSource messageSource) {
        MessageSourceUtil.messageSource = messageSource;
    }

    public static String get(String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }

}
