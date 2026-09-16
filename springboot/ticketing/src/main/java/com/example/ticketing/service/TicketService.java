package com.example.ticketing.service;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ticketing.domain.Reservation;
import com.example.ticketing.domain.Seat;
import com.example.ticketing.repository.ReservationRepository;
import com.example.ticketing.repository.SeatRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final RedissonClient redissonClient;

    public Long reserveTicketWithLock(Long userId, Long seatId) {
    	System.out.println("---21");
    	
        String lockKey = "LOCK:SEAT:" + seatId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean available = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if (!available) {
                throw new RuntimeException("현재 접속자가 많아 예매 요청을 처리할 수 없습니다. 다시 시도해주세요.");
            }

            return reserveTransaction(userId, seatId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("예매 진행 중 오류가 발생했습니다.");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional
    public Long reserveTransaction(Long userId, Long seatId) {
    	System.out.println("---22");
    	
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

        seat.reserve();

        Reservation reservation = new Reservation(userId, seat);
        reservationRepository.save(reservation);

        return reservation.getId();
    }
}



