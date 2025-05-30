package com.example.uai.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
public class CacheUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    // 使用构造器注入
    @Autowired
    public CacheUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 设置缓存（无过期时间）
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // 设置缓存并指定过期时间（秒）
    public void set(String key, Object value, long timeoutSeconds) {
        redisTemplate.opsForValue().set(key, value, timeoutSeconds, TimeUnit.SECONDS);
    }

    // 获取缓存（明确标注可能返回null）
    @Nullable
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 删除缓存（添加返回值）
    public boolean delete(String key) {
        Boolean result = redisTemplate.delete(key);
        return Boolean.TRUE.equals(result);
    }

    // 判断key是否存在
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // 新增：获取并删除（原子操作）
    @Nullable
    public Object getAndDelete(String key) {
        return redisTemplate.opsForValue().getAndDelete(key);
    }

    // 新增：设置过期时间
    public boolean expire(String key, long timeoutSeconds) {
        return Boolean.TRUE.equals(
                redisTemplate.expire(key, timeoutSeconds, TimeUnit.SECONDS)
        );
    }
}