package com.vishal.todo.services;

import com.vishal.todo.services.impl.RedisServiceImpl;

public interface RedisService {
    void setValue(String key, String value, long expirationSeconds);

    String getValue(String key);

    void deleteKey(String key);

    boolean isRedisHealthy();
}
