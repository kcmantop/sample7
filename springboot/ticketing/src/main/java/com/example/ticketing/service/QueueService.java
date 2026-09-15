package com.example.ticketing.service;

import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final StringRedisTemplate redisTemplate;
    private static final String QUEUE_KEY = "WAITING_QUEUE:PERFORMANCE:";

    public Long addQueue(Long performanceId, Long userId) {
        long now = System.currentTimeMillis();
        String key = QUEUE_KEY + performanceId;
        redisTemplate.opsForZSet().add(key, userId.toString(), now);
        return getRank(performanceId, userId);
    }

    public Long getRank(Long performanceId, Long userId) {
        String key = QUEUE_KEY + performanceId;
        Long rank = redisTemplate.opsForZSet().rank(key, userId.toString());
        return rank != null ? rank + 1 : -1L;
    }

    public Set<String> allowUsers(Long performanceId, long count) {
        String key = QUEUE_KEY + performanceId;
        Set<String> users = redisTemplate.opsForZSet().range(key, 0, count - 1);
        if (users != null && !users.isEmpty()) {
            redisTemplate.opsForZSet().remove(key, users.toArray());
        }
        return users;
    }
}



