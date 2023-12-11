/*
 * SPDX-License-Identifier: MIT
 * This file implements the Drain algorithm for log parsing.
 * Based on https://github.com/logpai/logparser/blob/master/logparser/Drain/Drain.py by LogPAI team
 */

package com.oppo.cloud.gpt.drain;

import java.util.concurrent.atomic.AtomicInteger;

public class SimpleIdGenerator implements IdGenerator {
    private AtomicInteger counter;

    public SimpleIdGenerator() {
        this.counter = new AtomicInteger(0);
    }

    public String next() {
        return String.valueOf(this.counter.incrementAndGet());
    }
}
