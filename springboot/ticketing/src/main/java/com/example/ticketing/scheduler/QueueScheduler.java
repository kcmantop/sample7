package com.example.ticketing.scheduler;

import java.util.List;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.ticketing.repository.PerformanceRepository;
import com.example.ticketing.service.QueueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueScheduler {

    private final QueueService queueService;
    private final PerformanceRepository performanceRepository;

    private static final long ALLOW_COUNT_PER_SECOND = 100L; // 초당 입장 허용 인원

    /**
     * 1초마다 실행되어 대기열 최상위 인원을 입장 처리
     */
    @Scheduled(fixedRate = 1000)
    public void processQueue() {
        // 현재 운영 중인 공연 목록 (실무에서는 활성화된 공연만 추출)
        List<Long> performanceIds = performanceRepository.findAllIds();

        for (Long performanceId : performanceIds) {
            Set<String> allowedUserIds = queueService.allowUsers(performanceId, ALLOW_COUNT_PER_SECOND);
            if (allowedUserIds != null && !allowedUserIds.isEmpty()) {
                log.info("공연 ID [{}]: {}명 예매 시스템 진입 승인", performanceId, allowedUserIds.size());
                // 필요 시 Redis에 '진입 허용 토큰(Pass Token)'을 부여하는 추가 로직 작성
            }
        }
    }
}