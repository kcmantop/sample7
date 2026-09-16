package com.example.ticketing.domain;

import java.time.LocalDateTime;

import com.example.ticketing.domain.Seat.SeatStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Performance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="title", nullable=false,length=100)
    private String title;
    
    @Column(nullable=false)
    private String description;

    @Column(name="start_time", nullable=false)
    private LocalDateTime startTime;
    
    @Column(name="end_time", nullable=false)
    private LocalDateTime endTime;
    
    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;
    
    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;

    @Builder
	public Performance(Long id, String title, String description, LocalDateTime startTime, LocalDateTime endTime,
			LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.startTime = startTime;
		this.endTime = endTime;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}     
}


