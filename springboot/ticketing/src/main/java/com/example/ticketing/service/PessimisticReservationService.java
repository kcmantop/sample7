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
    public void reserve(Long seatId, Users user) {
        Seat seat = seatRepository.findByIdWithPessimisticLock(seatId)
                .orElseThrow(() -> new IllegalArgumentException("좌석이 없습니다."));
        
        seat.reserve(); // 상태 변경 (AVAILABLE -> RESERVED)

        // 예약 내역 생성 및 저장
        Reservation reservation = Reservation.builder()
                .seat(seat)
                .user(user)
                .build();
        reservationRepository.save(reservation);
    }
}