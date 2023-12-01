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
