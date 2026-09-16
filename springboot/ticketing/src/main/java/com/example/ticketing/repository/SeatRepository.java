package com.example.ticketing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import com.example.ticketing.domain.Seat;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByPerformanceIdAndStatus(Long performanceId, Seat.SeatStatus status);

    // 동시성 제어를 위한 비관적 락 (SELECT FOR UPDATE)
    @Lock(LockModeType.PESSIMISTIC_WRITE)    
    @Query("SELECT s FROM Seat s WHERE s.id = :id")
    Optional<Seat> findByIdWithLock(@Param("id") Long id);
    
    // Fetch Join을 사용하여 N+1 문제 방지
    @Query("SELECT s FROM Seat s JOIN FETCH s.performance WHERE s.performance.id = :performanceId AND s.status = 'AVAILABLE'")
    List<Seat> findAvailableSeatsByPerformanceId(@Param("performanceId") Long performanceId);
}



