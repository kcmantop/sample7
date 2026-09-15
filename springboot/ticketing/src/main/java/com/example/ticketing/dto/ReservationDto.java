package com.example.ticketing.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ReservationDto {

    @Getter
    @NoArgsConstructor
    public static class CreateRequest {
        private Long seatId;
    }

    @Getter
    @Builder
    public static class Response {
        private Long reservationId;
        private Long userId;
        private Long seatId;
        private String seatNumber;
        private Long price;
        private LocalDateTime reservedAt;
    }
}