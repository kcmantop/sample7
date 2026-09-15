package com.example.ticketing.dto.response;

import com.example.ticketing.domain.Seat;

public record SeatResponseDto(
    Long seatId,
    String seatNumber,
    Long price,
    String status
) {
    // Entity -> DTO 정적 팩토리 메서드 (매우 훌륭한 패턴입니다!)
    public static SeatResponseDto from(Seat seat) {
        return new SeatResponseDto(
            seat.getId(),
            seat.getSeatNumber(),
            seat.getPrice(),
            seat.getStatus().name()
        );
    }
}