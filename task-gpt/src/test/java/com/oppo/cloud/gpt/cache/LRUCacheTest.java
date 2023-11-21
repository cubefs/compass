package com.oppo.cloud.gpt.cache;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LRUCacheTest {

    @Test
    public void testCache() {
        Cache<Integer, Integer> cache = new LRUCache<>(3);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        cache.put(4, 4);
        Assertions.assertFalse(cache.get(1).isPresent());
        Assertions.assertTrue(cache.get(2).isPresent());
    }

    @Test
    public void testSimpleCache() {
        Cache<Integer, Integer> cache = new SimpleCache<>();
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        cache.put(4, 4);
        Assertions.assertTrue(cache.get(1).isPresent());
        Assertions.assertTrue(cache.get(2).isPresent());
    }
}
