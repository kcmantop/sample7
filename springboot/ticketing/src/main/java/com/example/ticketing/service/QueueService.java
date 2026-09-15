package com.example.ticketing.service;

import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.example.ticketing.dto.QueueDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final StringRedisTemplate redisTemplate;
    private static final String QUEUE_KEY = "WAITING_QUEUE:PERFORMANCE:";

    private static final String QUEUE_KEY_PREFIX = "queue:performance:";
    private static final long AVG_PROCESSING_TIME_PER_USER_SEC = 2L; // 인당 평균 소요시간 추정치
    
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

    /**
     * 1. 대기열 등록 (ZADD)
     */
    public QueueDto.Response registerQueue(Long userId, Long performanceId) {
        String key = getQueueKey(performanceId);
        long currentTime = System.currentTimeMillis();

        // Redis ZSET에 추가 (Score = 현재 타임스탬프)
        redisTemplate.opsForZSet().add(key, String.valueOf(userId), currentTime);

        return getQueueStatus(userId, performanceId);
    }

    /**
     * 2. 내 대기 순번 및 예상 시간 조회 (ZRANK)
     */
    public QueueDto.Response getQueueStatus(Long userId, Long performanceId) {
        String key = getQueueKey(performanceId);

        // 내 앞의 대기자 수 조회 (0-based Index)
        Long rank = redisTemplate.opsForZSet().rank(key, String.valueOf(userId));

        if (rank == null) {
            throw new IllegalArgumentException("대기열에 존재하지 않는 사용자입니다.");
        }

        long expectedWaitTime = rank * AVG_PROCESSING_TIME_PER_USER_SEC;

        return QueueDto.Response.builder()
                .userId(userId)
                .performanceId(performanceId)
                .rank(rank)
                .expectedWaitTimeSeconds(expectedWaitTime)
                .build();
    }

    /**
     * 3. 상위 N명 진입 허용 (스케줄러/배치에서 호출)
     */
    public Set<String> allowUsers(Long performanceId, long count) {
        String key = getQueueKey(performanceId);

        // 가장 오래 대기한 상위 count명의 userId 조회 (ZRANGE)
        Set<String> allowedUserIds = redisTemplate.opsForZSet().range(key, 0, count - 1);

        if (allowedUserIds != null && !allowedUserIds.isEmpty()) {
            // 조회한 사용자들을 대기열에서 제거 (ZREM)
            redisTemplate.opsForZSet().remove(key, allowedUserIds.toArray());
        }

        return allowedUserIds;
    }

    /**
     * 4. 대기열 이탈 (이탈/취소 시 ZREM)
     */
    public void leaveQueue(Long userId, Long performanceId) {
        String key = getQueueKey(performanceId);
        redisTemplate.opsForZSet().remove(key, String.valueOf(userId));
    }

    private String getQueueKey(Long performanceId) {
        return QUEUE_KEY_PREFIX + performanceId;
    }
}






//package com.example.ticketing.service;
//
//import java.util.Set;
//
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Service;
//
//import lombok.RequiredArgsConstructor;
//
//@Service
//@RequiredArgsConstructor
//public class QueueService {
//
//    private final StringRedisTemplate redisTemplate;
//    private static final String QUEUE_KEY = "WAITING_QUEUE:PERFORMANCE:";
//
//    public Long addQueue(Long performanceId, Long userId) {
//        long now = System.currentTimeMillis();
//        String key = QUEUE_KEY + performanceId;
//        redisTemplate.opsForZSet().add(key, userId.toString(), now);
//        return getRank(performanceId, userId);
//    }
//
//    public Long getRank(Long performanceId, Long userId) {
//        String key = QUEUE_KEY + performanceId;
//        Long rank = redisTemplate.opsForZSet().rank(key, userId.toString());
//        return rank != null ? rank + 1 : -1L;
//    }
//
//    public Set<String> allowUsers(Long performanceId, long count) {
//        String key = QUEUE_KEY + performanceId;
//        Set<String> users = redisTemplate.opsForZSet().range(key, 0, count - 1);
//        if (users != null && !users.isEmpty()) {
//            redisTemplate.opsForZSet().remove(key, users.toArray());
//        }
//        return users;
//    }
//}



