package com.example.ticketing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.ticketing.dto.QueueDto;
import com.example.ticketing.service.QueueService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    /**
     * 대기열 등록 (입장 버튼 클릭 시)
     */
    @PostMapping("/register")
    public ResponseEntity<QueueDto.Response> registerQueue(
            @RequestParam(defaultValue = "1") Long userId, // 테스트용 임시 파라미터
            @RequestBody QueueDto.RegisterRequest request) {

        QueueDto.Response response = queueService.registerQueue(userId, request.getPerformanceId());
        return ResponseEntity.ok(response);
    }
//    @PostMapping("/register")
//    public ResponseEntity<QueueDto.Response> registerQueue(
//            @AuthenticationPrincipal Long userId,
//            @RequestBody QueueDto.RegisterRequest request) {
//
//        QueueDto.Response response = queueService.registerQueue(userId, request.getPerformanceId());
//        return ResponseEntity.ok(response);
//    }

    /**
     * 내 순번 조회 (폴링 방식)
     */
    @GetMapping("/status")
    public ResponseEntity<QueueDto.Response> getQueueStatus(
            @RequestParam(defaultValue = "1") Long userId, // @AuthenticationPrincipal 대신 임시 사용
            @RequestParam Long performanceId) {

        QueueDto.Response response = queueService.getQueueStatus(userId, performanceId);
        return ResponseEntity.ok(response);
    }
//    @GetMapping("/status")
//    public ResponseEntity<QueueDto.Response> getQueueStatus(
//            @AuthenticationPrincipal Long userId,
//            @RequestParam Long performanceId) {
//
//        QueueDto.Response response = queueService.getQueueStatus(userId, performanceId);
//        return ResponseEntity.ok(response);
//    }

    /**
     * 대기열 이탈 (뒤로가기/취소)
     */
    @DeleteMapping("/leave")
    public ResponseEntity<Void> leaveQueue(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long performanceId) {

        queueService.leaveQueue(userId, performanceId);
        return ResponseEntity.noContent().build();
    }
}