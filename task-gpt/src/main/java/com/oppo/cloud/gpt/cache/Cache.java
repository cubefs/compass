package com.oppo.cloud.gpt.cache;

import java.util.Optional;

/**
 * Cache interface
 */
public interface Cache<K, V> {

    boolean put(K key, V value);

    Optional<V> get(K key);

    boolean contains(K key);

    int size();

    boolean isEmpty();

    void clear();
}
