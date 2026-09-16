package com.example.ticketing.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ticketing.domain.Reservation;
import com.example.ticketing.domain.Seat;
import com.example.ticketing.domain.Users;
import com.example.ticketing.dto.ReservationDto;
import com.example.ticketing.repository.ReservationRepository;
import com.example.ticketing.repository.SeatRepository;
import com.example.ticketing.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final UsersRepository usersRepository;

    /**
     * 좌석 예매 (동시성 제어 적용)
     */
    @Transactional
    public ReservationDto.Response reserveSeat(Long userId, ReservationDto.CreateRequest request) {
        // 1. 회원 정보 조회
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 2. 비관적 락(Pessimistic Lock)을 적용하여 좌석 조회
        //    동시 요청 시 다른 트랜잭션은 락이 해제될 때까지 대기
        Seat seat = seatRepository.findByIdWithLock(request.getSeatId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

        // 3. 좌석 상태 변경 (이미 RESERVED 인 경우 IllegalStateException 발생)
        seat.reserve();

        // 4. 예약 정보 생성 및 저장
        Reservation reservation = Reservation.builder()
                .user(user)
                .seat(seat)                
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        // 5. 응답 DTO 반환
        return ReservationDto.Response.builder()
                .reservationId(savedReservation.getId())
                .userId(user.getId())
                .seatId(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .price(seat.getPrice())
                .reservedAt(savedReservation.getReservedAt())
                .build();
    }
    
    public ReservationDto.Response reserveSeat(Long seatId, Users user) {
        // 1. 회원 정보 조회
        //Users user = usersRepository.findById(userId)
        //        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 2. 비관적 락(Pessimistic Lock)을 적용하여 좌석 조회
        //    동시 요청 시 다른 트랜잭션은 락이 해제될 때까지 대기
        Seat seat = seatRepository.findByIdWithLock(seatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

        // 3. 좌석 상태 변경 (이미 RESERVED 인 경우 IllegalStateException 발생)
        seat.reserve();

        // 4. 예약 정보 생성 및 저장
        Reservation reservation = Reservation.builder()
                .user(user)
                .seat(seat)                
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        // 5. 응답 DTO 반환
        return ReservationDto.Response.builder()
                .reservationId(savedReservation.getId())
                .userId(user.getId())
                .seatId(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .price(seat.getPrice())
                .reservedAt(savedReservation.getReservedAt())
                .build();
    }
}








