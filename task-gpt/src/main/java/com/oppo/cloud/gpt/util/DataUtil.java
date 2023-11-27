package com.oppo.cloud.gpt.util;

import com.alibaba.fastjson2.JSON;
import com.oppo.cloud.common.domain.LogMessage;

public class DataUtil {
    public static LogMessage decode(String message) {
        return JSON.parseObject(message, LogMessage.class);
    }
}
