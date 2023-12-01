/*
 * Copyright 2023 OPPO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
