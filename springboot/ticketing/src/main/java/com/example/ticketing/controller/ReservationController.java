package com.example.ticketing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.ticketing.dto.ReservationDto;
import com.example.ticketing.service.ReservationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationDto.Response> reserveSeat(
            @AuthenticationPrincipal Long userId,
            @RequestBody ReservationDto.CreateRequest request) {

        ReservationDto.Response response = reservationService.reserveSeat(userId, request);
        return ResponseEntity.ok(response);
    }
}