package com.example.ticketing.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ticketing.domain.Reservation;
import com.example.ticketing.domain.ReservationStatus;
import com.example.ticketing.domain.Seat;
import com.example.ticketing.domain.Users;
import com.example.ticketing.repository.ReservationRepository;
import com.example.ticketing.repository.SeatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PessimisticReservationService {

    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public void reserveSeat(Long seatId, Users user) {
        // 1. SELECT ... FOR UPDATE 로 락 획득 (대기 발생)
        Seat seat = seatRepository.findByIdWithLock(seatId)
                .orElseThrow(() -> new IllegalArgumentException("좌석이 존재하지 않습니다."));

        if (seat.isReserved()) {
            throw new IllegalStateException("이미 예약된 좌석입니다.");
        }

        // 2. 점유 처리 및 저장
        seat.reserve();
        reservationRepository.save(new Reservation(user, seat, ReservationStatus.CONFIRMED));
    }
}