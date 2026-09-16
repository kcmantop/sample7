package com.example.ticketing.concurrency;

import com.example.ticketing.domain.Seat;
import com.example.ticketing.repository.SeatRepository;
import com.example.ticketing.domain.Users;
import com.example.ticketing.repository.UsersRepository;
import com.example.ticketing.facade.RedissonLockReservationFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConcurrencyTest {

    @Autowired
    private RedissonLockReservationFacade reservationFacade; // 테스트 대상 (비관적/낙관적/분산락 각각 변경)

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Test
    @DisplayName("동시에 100명이 동일한 좌석을 예매 시도하면 1명만 성공해야 한다")
    void reserveSeat_concurrency_100_requests() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        Seat seat = seatRepository.save(new Seat(/* 초기 좌석 생성 */));
        Users user = usersRepository.save(new Users(/* 테스트 유저 생성 */));

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    reservationFacade.reserveSeat(seat.getId(), user);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 100개 스레드가 모두 완료될 때까지 대기

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(99);
    }
}