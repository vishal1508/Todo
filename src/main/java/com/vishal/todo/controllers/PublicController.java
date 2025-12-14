package com.vishal.todo.controllers;

import com.vishal.todo.services.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private RedisService redisService;

    @GetMapping("/health/redis")
    public ResponseEntity<?> checkRedisHealth() {

        boolean redisOK = redisService.isRedisHealthy();

        Map<String, Object> response = new HashMap<>();
        response.put("redis_status", redisOK ? "UP" : "DOWN");

        if (redisOK) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(500).body(response);
        }
    }

}
