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

package com.oppo.cloud.gpt.drain;

import com.oppo.cloud.gpt.cache.Cache;
import com.oppo.cloud.gpt.cache.LRUCache;
import com.oppo.cloud.gpt.cache.SimpleCache;

/**
 * Log Cluster Cache implementation.
 */
public class LogClusterCache {
    /**
     * Caching Cluster
     */
    private Cache<String, LogCluster> cache;

    public LogClusterCache() {
        this.cache = new SimpleCache<>();
    }

    public LogClusterCache(int size) {
        this.cache = size >= 0 ? new LRUCache<>(size) : new SimpleCache<>();
    }

    /**
     * cache log cluster.
     *
     * @param id
     * @param logCluster
     * @return
     */
    public boolean put(String id, LogCluster logCluster) {
        return this.cache.put(id, logCluster);
    }

    /**
     * get log cluster by id key.
     *
     * @param id
     * @return
     */
    public LogCluster get(String id) {
        return this.cache.get(id).orElse(null);
    }

    /**
     * check whether cluster exists.
     *
     * @param id
     * @return
     */
    public boolean contains(String id) {
        return this.cache.contains(id);
    }
}
