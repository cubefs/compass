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

package com.oppo.cloud.common.service.impl;

import com.oppo.cloud.common.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis操作实现类
 */
@Slf4j
public class RedisServiceImpl implements RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Set the value and expiration timeout for key
     */
    @Override
    public Boolean set(String key, Object value, long timeout) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            log.error("failed to set key: {} and value: {}, timeout: {}, err: {}", key, value, timeout, e.getMessage());
            return false;
        }
    }

    /**
     * Set value for key
     */
    @Override
    public Boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("failed to set key: {} and value: {}, err: {}", key, value, e.getMessage());
            return false;
        }
    }

    /**
     * Get the value of key
     */
    @Override
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("failed to get value, key: {}, err: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Delete given key
     */
    @Override
    public Boolean del(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("failed to delete key: {}, err: {}", key, e.getMessage());
            return false;
        }
    }

    /**
     * Delete given keys
     */
    @Override
    public Long del(List<String> keys) {
        try {
            return redisTemplate.delete(keys);
        } catch (Exception e) {
            log.error("failed to delete keys: {}, err: {}", keys, e.getMessage());
            return 0L;
        }
    }

    /**
     * Determine if given key exists
     */
    @Override
    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("failed to check key: {}, err: {}", key, e.getMessage());
            return false;
        }

    }

    /**
     * Set the value of a hash hashKey
     * redis: HSET command
     */
    @Override
    public Boolean hSet(String key, Object hashKey, Object value) {
        try {
            redisTemplate.opsForHash().put(key, hashKey, value);
            return true;
        } catch (Exception e) {
            log.error("failed to put hash key, key: {}, hashKey: {}, value: {}", key, hashKey, value);
            return false;
        }
    }

    /**
     * Get entire hash stored at key
     * redis: HGETALL command
     */
    @Override
    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("failed to hgetall, key: {}, err: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Delete given hash hashKeys
     * redis: HDEL command
     */
    @Override
    public Long hDel(String key, Object... hashKeys) {
        try {
            return redisTemplate.opsForHash().delete(key, hashKeys);
        } catch (Exception e) {
            log.error("failed to hdel, key: {}, hashKeys: {}, err: {}", key, hashKeys, e.getMessage());
            return 0L;
        }
    }

    /**
     * Get size of hash at key
     * redis: HLEN command
     */
    @Override
    public Long hLen(String key) {
        try {
            return redisTemplate.opsForHash().size(key);
        } catch (Exception e) {
            log.error("failed to hlen, key: {}, err: {}", key, e.getMessage());
            return 0L;
        }
    }

    /**
     * Get the size of list stored at key
     * redis: LLEN command
     */
    @Override
    public Long lLen(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error("failed to llen: {}, err: {}", key, e.getMessage());
            return 0L;
        }
    }

    /**
     * Prepend value to key
     */
    @Override
    public Long lLeftPush(String key, Object value) {
        try {
            return redisTemplate.opsForList().leftPush(key, value);
        } catch (Exception e) {
            log.error("failed to lPush, key: {}, value: {}, err: {}", key, value, e.getMessage());
            return 0L;
        }
    }

    /**
     * Append value to key
     */
    @Override
    public Long lRightPush(String key, Object value) {
        try {
            return redisTemplate.opsForList().rightPush(key, value);
        } catch (Exception e) {
            log.error("failed to lRightPush, key: {}, value: {}, err: {}", key, value, e.getMessage());
            return 0L;
        }
    }

    /**
     * Removes and returns first element in list stored at key
     */
    @Override
    public Object lLeftPop(String key) {
        try {
            return redisTemplate.opsForList().leftPop(key);
        } catch (Exception e) {
            log.error("failed to lLeftPop, key: {}, err: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Removes and returns last element in list stored at key
     */
    @Override
    public Object lRightPop(String key) {
        try {
            return redisTemplate.opsForList().rightPop(key, -1L, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("failed to lRightPop, key: {}, err: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Removes and returns last element in list stored at key
     */
    @Override
    public Object lRightPop(String key, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.opsForList().rightPop(key, timeout, unit);
        } catch (Exception e) {
            log.error("failed to lRightPop, key: {}, err: {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Execute lua script
     */
    @Override
    public Object executeScript(RedisScript<Object> script, List<String> key, Object... args) {
        return redisTemplate.execute(script, key, args);
    }

    /**
     * Add value to a sorted set at key, or update its score if it already exists.
     */
    @Override
    public Boolean zSetAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * Get elements where score is between min and max from sorted set.
     */
    @Override
    public Set<Object> zSetRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * Remove values from sorted set. Return number of removed elements.
     */
    @Override
    public Long zSetRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * Set key to hold the string value and expiration timeout if key is absent
     */
    @Override
    public Boolean acquireLock(String key, String value, Long timeout) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS);
    }
}
