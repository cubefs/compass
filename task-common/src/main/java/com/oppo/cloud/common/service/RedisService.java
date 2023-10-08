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

package com.oppo.cloud.common.service;

import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis operation service
 */
public interface RedisService {

    /**
     * Set the value and expiration timeout for key
     */
    Boolean set(String key, Object value, long time);

    /**
     * Set value for key
     */
    Boolean set(String key, Object value);

    /**
     * Get the value of key
     */
    Object get(String key);

    /**
     * Delete given key
     */
    Boolean del(String key);

    /**
     * Delete given keys
     */
    Long del(List<String> keys);

    /**
     * Determine if given key exists
     */
    Boolean hasKey(String key);

    /**
     * Set the value of a hash hashKey
     */
    Boolean hSet(String key, Object hashKey, Object value);

    /**
     * Get entire hash stored at key
     */
    Map<Object, Object> hGetAll(String key);

    /**
     * Delete given hash hashKeys
     */
    Long hDel(String key, Object... hashKeys);

    /**
     * Get size of hash at key
     * redis: HLEN command
     */
    Long hLen(String key);

    /**
     * Get the size of list stored at key
     * redis: LLEN command
     */
    Long lLen(String key);

    /**
     * Prepend value to key
     * Redis: LPUSH comannd
     */
    Long lLeftPush(String key, Object value);

    /**
     * Append value to key
     * Redis: LPUSH comannd
     */
    Long lRightPush(String key, Object value);

    /**
     * Removes and returns first element in list stored at key
     */
    Object lLeftPop(String key);

    /**
     * Removes and returns last element in list stored at key
     */
    Object lRightPop(String key);

    /**
     * Removes and returns last element from lists stored at key
     */
    Object lRightPop(String key, long timeout, TimeUnit unit);

    /**
     * execute lua script
     */
    Object executeScript(RedisScript<Object> script, List<String> key, Object... args);

    /**
     * Add value to a sorted set at key, or update its score if it already exists.
     */
    Boolean zSetAdd(String key, Object value, double score);

    /**
     * Add value to a sorted set at key, or update its score if it already exists.
     */
    Set<Object> zSetRangeByScore(String key, double min, double max);

    /**
     * Remove values from sorted set. Return number of removed elements.
     */
    public Long zSetRemove(String key, Object... values);

    /**
     * Set key to hold the string value and expiration timeout if key is absent
     */
    Boolean acquireLock(String key, String value, Long timeout);

}
