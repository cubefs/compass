package com.oppo.cloud.gpt.cache;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple cache without limited size.
 * @param <K>
 * @param <V>
 */
public class SimpleCache<K, V> implements Cache<K, V> {

    private Map<K, V> cache;

    public SimpleCache() {
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public void clear() {
        this.cache.clear();
    }

    @Override
    public boolean isEmpty() {
        return this.cache.isEmpty();
    }

    @Override
    public boolean put(K key, V value) {
        this.cache.put(key, value);
        return true;
    }

    @Override
    public boolean contains(K key) {
        return this.cache.containsKey(key);
    }

    @Override
    public int size() {
        return this.cache.size();
    }

    @Override
    public Optional<V> get(K key) {
        V value = this.cache.get(key);
        if (value != null) {
            return Optional.of(value);
        }
        return Optional.empty();
    }
}
