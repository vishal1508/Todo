package com.vishal.todo.services.impl;

import com.vishal.todo.services.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisServiceImpl implements RedisService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void setValue(String key, String value, long expirationSeconds) {
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(expirationSeconds));
    }

    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    public boolean isRedisHealthy() {
        try {
            String testKey = "health_check";
            String testValue = "OK";

            // Write test
            redisTemplate.opsForValue().set(testKey, testValue);

            // Read test
            String value = redisTemplate.opsForValue().get(testKey);

            return "OK".equals(value);
        } catch (Exception e) {
            return false;
        }
    }

}
