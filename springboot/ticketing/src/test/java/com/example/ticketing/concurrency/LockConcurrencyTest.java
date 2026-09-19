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
import com.example.ticketing.facade.OptimisticLockReservationFacade;
import com.example.ticketing.facade.RedissonLockReservationFacade;
import com.example.ticketing.repository.PerformanceRepository;
import com.example.ticketing.repository.ReservationRepository;
import com.example.ticketing.repository.SeatRepository;
import com.example.ticketing.repository.UsersRepository;
import com.example.ticketing.service.OptimisticReservationService;
import com.example.ticketing.service.PessimisticReservationService;

@SpringBootTest
class LockConcurrencyTest {

	@Autowired private PessimisticReservationService pessimisticService;
    @Autowired private OptimisticReservationService optimisticService;
    @Autowired private RedissonLockReservationFacade redissonFacade;
    @Autowired private SeatRepository seatRepository;
    @Autowired private UsersRepository usersRepository;
    @Autowired private ReservationRepository reservationRepository;

    private Long targetSeatId;
    private Users testUser;
    
    @Autowired
    private PerformanceRepository performanceRepository;
    
    @Autowired
    OptimisticLockReservationFacade optimisticLockFacade;
    
    @BeforeEach
    void setUp() {        
    	System.out.println("---51 LocalDateTime.now())" + LocalDateTime.now());
    	
        // 1. Foreign Key 참조 관계 역순으로 삭제
        reservationRepository.deleteAllInBatch(); // 👈 가장 먼저 지워야 함!
        seatRepository.deleteAllInBatch();
        performanceRepository.deleteAllInBatch();
        usersRepository.deleteAllInBatch();
        
        System.out.println("---52");
        
    	// 1. Users 객체 생성 및 DB 저장 (email 필드 필수 지정)
        Users user = Users.builder()
        		.password("1111") // email 필드 명시
        		.email("test_" + System.currentTimeMillis() + "@example.com")// email 필드 명시
                .name("테스트유저")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();        
        
        System.out.println("---53");
        usersRepository.save(user);
        
        System.out.println("---54 userCreatedAt() " + user.getCreatedAt());
        
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
    @DisplayName("1. 비관적 락 동시성 테스트 (Users 포함)")
    void pessimisticLock_test() throws InterruptedException {   	
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // testUser 전달
                    pessimisticService.reserve(targetSeatId, testUser);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(99);
        
        // DB에 실제 예약건이 1개 생성되었는지 검증
        assertThat(reservationRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("2. 낙관적 락 동시성 테스트")
    void optimisticLock_test() throws InterruptedException {
    	System.out.println("---55");
    	
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        
        System.out.println("---56");

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                	System.out.println("---57");
                	
                    // 파사드 사용 시
                    //optimisticLockFacade.reserve(targetSeatId, testUser);
                	optimisticService.reserve(targetSeatId, testUser);
                    successCount.incrementAndGet();
                    
                    System.out.println("---58");
                    
                } catch (Exception e) {
                	//e.printStackTrace();
                	
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // 1번만 성공하고 나머지 성공 횟수 및 예약 건수 검증
        //assertThat(successCount.get()).isEqualTo(1);
        assertThat(successCount.get())
        .as("성공한 예약 건수는 정확히 1이어야 합니다 (실제 성공: %d, 실패: %d)", 
            successCount.get(), failCount.get())
        .isEqualTo(1);
        assertThat(reservationRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("3. 레디스 분산 락 동시성 테스트 (Users 포함)")
    void redissonLock_test() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    redissonFacade.reserve(targetSeatId, testUser);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(99);
        assertThat(reservationRepository.count()).isEqualTo(1);
    }
}