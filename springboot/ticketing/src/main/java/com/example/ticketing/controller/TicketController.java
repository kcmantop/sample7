package com.example.ticketing.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ticketing.dto.common.ApiResponse;
import com.example.ticketing.dto.request.QueueRequestDto;
import com.example.ticketing.dto.request.TicketReserveRequestDto;
import com.example.ticketing.dto.response.SeatResponseDto;
import com.example.ticketing.service.QueueService;
import com.example.ticketing.service.SeatService; // Service 추가
import com.example.ticketing.service.TicketService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final QueueService queueService;
    private final SeatService seatService; // Repository 대신 Service 주입

    /**
     * 1. 대기열 등록
     */
    @PostMapping("/queue")
    public ResponseEntity<ApiResponse<Long>> enterQueue(@RequestBody QueueRequestDto request) {
        Long rank = queueService.addQueue(request.performanceId(), request.userId());
        return ResponseEntity.ok(ApiResponse.success("대기열 등록 완료", rank));
    }

    /**
     * 2. 예약 가능 좌석 조회 (DTO로 변환하여 응답)
     */
    @GetMapping("/seats")
    public ResponseEntity<ApiResponse<List<SeatResponseDto>>> getAvailableSeats(@RequestParam Long performanceId) {
        List<SeatResponseDto> seats = seatService.getAvailableSeats(performanceId);
        return ResponseEntity.ok(ApiResponse.success(seats));
    }

    /**
     * 3. 티켓 예매 요청 (분산 락 적용)
     */
    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<Long>> reserveTicket(@RequestBody TicketReserveRequestDto request) {
        Long reservationId = ticketService.reserveTicketWithLock(request.userId(), request.seatId());
        return ResponseEntity.ok(ApiResponse.success("예매가 성공적으로 완료되었습니다.", reservationId));
    }
}