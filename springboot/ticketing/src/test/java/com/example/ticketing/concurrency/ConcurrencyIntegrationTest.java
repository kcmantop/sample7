package com.example.ticketing.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.ticketing.domain.Seat;
import com.example.ticketing.domain.Users;
import com.example.ticketing.facade.RedissonLockReservationFacade;
import com.example.ticketing.repository.SeatRepository;
import com.example.ticketing.repository.UsersRepository;

@SpringBootTest
class ConcurrencyIntegrationTest {

    @Autowired
    private RedissonLockReservationFacade reservationFacade; // 테스트할 대상 Facade/Service

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UsersRepository usersRepository;

    private Long targetSeatId;
    private Users testUser;

    @BeforeEach
    void setUp() {
        // 테스트용 데이터 초기화
        Seat seat = seatRepository.save(new Seat(/* 초기 상태 지정 */));
        this.targetSeatId = seat.getId();
        this.testUser = usersRepository.save(new Users(/* 테스트 유저 생성 */));
    }

    @Test
    @DisplayName("100개의 요청이 동시에 동일한 좌석을 예매 시도하면 단 1명만 성공해야 한다")
    void reserveSeat_concurrency_100_threads() throws InterruptedException {
        // given
        int totalRequests = 100;
        int threadPoolSize = 32; // 동시 실행 스레드 수
        
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        CountDownLatch latch = new CountDownLatch(totalRequests);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < totalRequests; i++) {
            executorService.submit(() -> {
                try {
                    reservationFacade.reserveSeat(targetSeatId, testUser);
                    successCount.incrementAndGet(); // 성공 카운트
                } catch (Exception e) {
                    failCount.incrementAndGet(); // 실패/예외 카운트 (락 획득 실패, 이미 예매됨 등)
                } finally {
                    latch.countDown(); // 작업 완료 신호
                }
            });
        }

        latch.await(); // 100개 스레드의 작업 완료까지 대기

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(99);
    }
}