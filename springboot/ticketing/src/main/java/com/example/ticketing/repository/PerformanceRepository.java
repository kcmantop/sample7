package com.example.ticketing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ticketing.domain.Performance;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {

    /**
     * 등록된 모든 공연의 ID 목록을 조회 (대기열 스케줄러 등에서 활용)
     */
    @Query("SELECT p.id FROM Performance p")
    List<Long> findAllIds();
}