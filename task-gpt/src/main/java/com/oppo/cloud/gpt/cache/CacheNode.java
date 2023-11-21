package com.oppo.cloud.gpt.cache;

import lombok.Data;

/**
 * Cache node.
 * @param <K>
 * @param <V>
 */
@Data
public class CacheNode<K, V> {

    private K key;

    private V value;

    public CacheNode(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
