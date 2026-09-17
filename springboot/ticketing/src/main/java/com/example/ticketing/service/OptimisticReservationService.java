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
    public void reserve(Long seatId, Users user) {
    	System.out.println("---61");
    	
        Seat seat = seatRepository.findByIdWithOptimisticLock(seatId)
                .orElseThrow(() -> new IllegalArgumentException("좌석이 없습니다."));
        
        System.out.println("---62");
        
        seat.reserve();
        
        System.out.println("---63");

        Reservation reservation = Reservation.builder()
                .seat(seat)
                .user(user)
                .build();
        reservationRepository.save(reservation);
    }
}