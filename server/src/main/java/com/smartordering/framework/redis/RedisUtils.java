package com.smartordering.framework.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis utility wrapper
 *
 * @author smartordering
 */
@Component
@RequiredArgsConstructor
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    /** Hash: set field */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /** Hash: get field */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /** Hash: get all fields */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /** Hash: delete field */
    public void hDelete(String key, String field) {
        redisTemplate.opsForHash().delete(key, field);
    }

    /** Set TTL */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /** Delete key */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }
}