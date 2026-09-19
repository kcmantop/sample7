package com.example.ticketing.facade;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import com.example.ticketing.domain.Users;
import com.example.ticketing.dto.ReservationDto;
import com.example.ticketing.service.PessimisticReservationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedissonLockReservationFacade {
    private final RedissonClient redissonClient;
    private final PessimisticReservationService reservationService;

    public ReservationDto.Response reserve(Long seatId, Users user) {
    	ReservationDto.Response response;
    	
        RLock lock = redissonClient.getLock("lock:seat:" + seatId);

        try {
            boolean available = lock.tryLock(1, 3, TimeUnit.SECONDS);
            if (!available) {
                throw new IllegalStateException("락 획득 실패");
            }
            response = reservationService.reserve(seatId, user);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        
        response = ReservationDto.Response.builder()
        	    .reservationId(0L)
        	    .seatNumber("")
        	    .userId(0L)
        	    .build();
        
        return response;
    }
}