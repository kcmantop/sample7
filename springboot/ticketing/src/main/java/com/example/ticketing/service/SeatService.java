package com.example.ticketing.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ticketing.dto.response.SeatResponseDto;
import com.example.ticketing.repository.SeatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;

    public List<SeatResponseDto> getAvailableSeats(Long performanceId) {
        return seatRepository.findAvailableSeatsByPerformanceId(performanceId)
                .stream()
                .map(SeatResponseDto::from)
                .toList();
    }
}