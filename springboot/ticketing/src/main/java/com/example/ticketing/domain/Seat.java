package com.example.ticketing.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "seat", indexes = {
    @Index(name = "idx_performance_status", columnList = "performance_id, status")
})
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id")
    private Performance performance;
    
    @Version // 낙관적 락을 위한 버전 관리
    private Long version;

    private boolean isReserved;

    //@Column(name="performance_id", nullable=false)
    //private Long performanceId;
    
    @Column(name="seat_number",nullable=false)    
    private String seatNumber;
    
    @Column(nullable=false)
    private Long price;
        
    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    @Column(name="created_at",nullable=false)
    private LocalDateTime createdAt;
    
    @Column(name="updated_at",nullable=false)
    private LocalDateTime updatedAt;    

    public enum SeatStatus {
        AVAILABLE, RESERVED
    }
    
//    public Seat(Performance performance, String seatNumber, Long price) {
//        this.performance = performance;
//        this.seatNumber = seatNumber;
//        this.price = price;
//        this.status = SeatStatus.AVAILABLE;
//    }
    
    @Builder
	public Seat(Long id, Performance performance, String seatNumber, Long price, SeatStatus status,
			LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.performance = performance;		
		this.seatNumber = seatNumber;
		this.price = price;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
	
	public void reserve() {
        if (this.status == SeatStatus.RESERVED) {
            throw new IllegalStateException("이미 예매 완료된 좌석입니다.");
        }
        this.status = SeatStatus.RESERVED;
        this.isReserved = true;
    }
}

