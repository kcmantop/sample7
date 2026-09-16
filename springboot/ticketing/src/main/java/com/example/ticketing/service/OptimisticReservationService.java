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
public class OptimisticReservationService {

    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public void reserveSeat(Long seatId, Users user) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("좌석이 존재하지 않습니다."));

        if (seat.isReserved()) {
            throw new IllegalStateException("이미 예약된 좌석입니다.");
        }

        seat.reserve(); // Commit 시점에 version 비교 후 다르면 Exception 발생
        reservationRepository.save(new Reservation(user, seat, ReservationStatus.CONFIRMED));
    }
}