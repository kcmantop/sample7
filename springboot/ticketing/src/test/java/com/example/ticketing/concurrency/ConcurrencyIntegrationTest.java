package com.example.ticketing.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.ticketing.domain.Performance;
import com.example.ticketing.domain.Seat;
import com.example.ticketing.domain.Seat.SeatStatus;
import com.example.ticketing.domain.Users;
import com.example.ticketing.facade.RedissonLockReservationFacade;
import com.example.ticketing.repository.PerformanceRepository;
import com.example.ticketing.repository.ReservationRepository;
import com.example.ticketing.repository.SeatRepository;
import com.example.ticketing.repository.UsersRepository;
import com.example.ticketing.service.ReservationService;

@SpringBootTest
class ConcurrencyIntegrationTest {

    @Autowired
    private RedissonLockReservationFacade reservationFacade; // 테스트할 대상 Facade/Service

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UsersRepository usersRepository;
    
    @Autowired
    private PerformanceRepository performanceRepository;

    private Long targetSeatId;
    private Users testUser;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {        
    	System.out.println("---1");
    	
        // 1. Foreign Key 참조 관계 역순으로 삭제
        reservationRepository.deleteAllInBatch(); // 👈 가장 먼저 지워야 함!
        seatRepository.deleteAllInBatch();
        performanceRepository.deleteAllInBatch();
        usersRepository.deleteAllInBatch();
        
    	// 1. Users 객체 생성 및 DB 저장 (email 필드 필수 지정)
        Users user = Users.builder()
        		.password("1111") // email 필드 명시
        		.email("test_" + System.currentTimeMillis() + "@example.com")// email 필드 명시
                .name("테스트유저")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();        
        usersRepository.save(user);
        
    	// 1. Performance(공연) 객체 생성 및 DB 저장
        Performance performance = Performance.builder()
                .title("테스트 공연")
                .description("공연 설명입니다.")     // 부분 추가
                .startTime(LocalDateTime.now()) // 추가
                .endTime(LocalDateTime.now()) // 추가
                .createdAt(LocalDateTime.now()) // 추가 
                .updatedAt(LocalDateTime.now()) // 추가 
                .build();
        performanceRepository.save(performance);

        // 2. Seat 객체 생성 시 performance 지정
        Seat seat = Seat.builder()
                .performance(performance) // 👈 연관관계 주입 (필수)
                .seatNumber("A1")
                .status(SeatStatus.AVAILABLE)
                .price(50000L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    	
        // 테스트용 데이터 초기화
        //Seat seat = seatRepository.save(new Seat(/* 초기 상태 지정 */));
        seatRepository.save(seat);
        this.targetSeatId = seat.getId();
        //this.testUser = usersRepository.save(new Users(/* 테스트 유저 생성 */));
        this.testUser = usersRepository.save(user);
    }

    @Test
    @DisplayName("100개의 요청이 동시에 동일한 좌석을 예매 시도하면 단 1명만 성공해야 한다")
    void reserveSeat_concurrency_100_threads() throws InterruptedException {
    	System.out.println("---2");
    	
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
                    //reservationService.reserveSeat(targetSeatId, testUser);
                    
                    successCount.incrementAndGet(); // 성공 카운트
                } catch (Exception e) {
                	System.out.println("[예약 실패 원인]: " + e.getClass().getSimpleName() + " - " + e.getMessage());
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