package com.example.ticketing.facade;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import com.example.ticketing.domain.Users;
import com.example.ticketing.service.ReservationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedissonLockReservationFacade {

    private final RedissonClient redissonClient;
    private final ReservationService reservationService;

    public void reserveSeat(Long seatId, Users user) {
    	System.out.println("---3");
        RLock lock = redissonClient.getLock("lock:seat:" + seatId);

        try {
        	System.out.println("---4");
        	
            // waitTime: 락 획득 대기 시간(3초), leaseTime: 락 자동 해제 시간(2초)
            boolean available = lock.tryLock(3, 2, TimeUnit.SECONDS);
            
            System.out.println("---5");

            if (!available) {
            	System.out.println("---6");
            	
                throw new IllegalStateException("현재 요청이 많아 락 획득에 실패했습니다.");
            }
            
            System.out.println("---7");

            // 락 획득 성공 시 비즈니스 로직 수행 (별도 트랜잭션 분리 필요)
            reservationService.reserveSeat(seatId, user);
            
            System.out.println("---8");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            // 내가 획득한 락이고, 현재 점유 중인 경우에만 해제
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}