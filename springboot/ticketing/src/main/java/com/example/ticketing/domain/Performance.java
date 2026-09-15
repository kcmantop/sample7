package com.example.ticketing.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private String descriptiont;

    @Column(name="start_time", nullable=false)
    private LocalDateTime startTime;
    
    @Column(name="end_time", nullable=false)
    private LocalDateTime endTime;
    
    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;
    
    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;

	public Performance(Long id, String title, String descriptiont, LocalDateTime startTime, LocalDateTime endTime,
			LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.title = title;
		this.descriptiont = descriptiont;
		this.startTime = startTime;
		this.endTime = endTime;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}     
}


