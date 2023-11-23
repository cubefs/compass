package com.oppo.cloud.gpt.util;

import com.alibaba.fastjson2.JSON;
import com.oppo.cloud.gpt.drain.LogCluster;

public class DataUtil {
    public static LogCluster decode(String message) {
        return JSON.parseObject(message, LogCluster.class);
    }
}
