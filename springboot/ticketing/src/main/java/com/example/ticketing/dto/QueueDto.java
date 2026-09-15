package com.example.ticketing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class QueueDto {

    @Getter
    @NoArgsConstructor
    public static class RegisterRequest {
        private Long performanceId;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Response {
        private Long userId;
        private Long performanceId;
        private Long rank;          // 앞 대기자 수 (0이면 입장 가능)
        private Long expectedWaitTimeSeconds; // 예상 대기 시간(초)
    }
}