package com.oppo.cloud.gpt.util;

import com.oppo.cloud.gpt.drain.IdGenerator;

import java.util.UUID;

/**
 * Generate Id for log cluster
 */
public class UUIDIdGenerator implements IdGenerator {

    @Override
    public String next() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
