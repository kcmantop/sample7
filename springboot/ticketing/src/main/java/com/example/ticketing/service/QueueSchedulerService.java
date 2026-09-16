package com.example.ticketing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueSchedulerService {

    private final StringRedisTemplate redisTemplate;

    private static final String WAITING_QUEUE_KEY = "queue:waiting"; // 대기열 (ZSET)
    private static final String ACTIVE_TOKEN_PREFIX = "token:active:"; // 활성화된 토큰 (String/Key)
    private static final long PROCESS_SIZE = 100; // 1회 처리 인원
    private static final long TOKEN_TTL_MINUTES = 10; // 발급된 토큰 유효시간

    /**
     * 5초마다 실행되어 대기열 상위 100명에게 예매 권한(Token) 부여
     */
    @Scheduled(fixedRate = 5000)
    public void processQueue() {
        // 1. 대기열(ZSET)에서 스코어가 가장 낮은(먼저 들어온) 100명 추출
        Set<String> tokensToProcess = redisTemplate.opsForZSet()
                .range(WAITING_QUEUE_KEY, 0, PROCESS_SIZE - 1);

        if (tokensToProcess == null || tokensToProcess.isEmpty()) {
            return;
        }

        for (String token : tokensToProcess) {
            // 2. 예매 권한(Active Token) 발급 (TTL 설정으로 자동 만료 처리)
            String activeKey = ACTIVE_TOKEN_PREFIX + token;
            redisTemplate.opsForValue().set(activeKey, "ACTIVE", TOKEN_TTL_MINUTES, TimeUnit.MINUTES);

            // 3. 대기열 ZSET에서 해당 토큰 제거
            redisTemplate.opsForZSet().remove(WAITING_QUEUE_KEY, token);
        }

        log.info("대기열 처리 완료: {} 명의 토큰이 활성화되었습니다.", tokensToProcess.size());
    }
}