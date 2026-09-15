package com.example.ticketing.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.ticketing.dto.ReservationDto;
import com.example.ticketing.repository.ReservationRepository;

@SpringBootTest
class ReservationConcurrencyTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("100명의 유저가 동시에 동일 좌석을 예매하면 1명만 성공해야 한다.")
    void reserveSeat_concurrency_test() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        Long targetSeatId = 1L;

        for (int i = 0; i < threadCount; i++) {
            long userId = i + 1; // 테스트용 유저 ID들
            executorService.submit(() -> {
                try {
                    ReservationDto.CreateRequest request = new ReservationDto.CreateRequest();
                    // Reflection 또는 Setter로 seatId 설정 (필요 시 DTO 생성자 활용)
                    reservationService.reserveSeat(userId, request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 종료 대기

        // 검증: 성공 1건, 실패 99건, DB 저장 레코드 1건
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);
        assertThat(reservationRepository.count()).isEqualTo(1);
    }
}