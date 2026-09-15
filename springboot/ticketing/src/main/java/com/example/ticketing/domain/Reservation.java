package com.example.ticketing.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false, unique = true)
    private Seat seat;

    @Column(name = "reserved_at", nullable = false, updatable = false)
    private LocalDateTime reservedAt;
    
    @Column(name="created_at",nullable=false)
    private LocalDateTime createdAt;
  
    @Column(name="updated_at",nullable=false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.reservedAt = LocalDateTime.now();
    }

    @Builder
    public Reservation(Users user, Seat seat) {
        this.user = user;
        this.seat = seat;
    }
    
    public Reservation(Long userId, Seat seat) {
        this.id = userId;
        this.seat = seat;
        this.reservedAt = LocalDateTime.now();
    }
}
















//
//package com.example.ticketing.domain;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Getter
//@NoArgsConstructor
//public class Reservation {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name="user_id",nullable=false)
//    private Long userId;
//    
//    @Column(name="performance_id",nullable=false)
//    private Long preformanceId;
//    
//    //@Column(name="seat_id",nullable=false)
//    //private Long seatId;
//    
//    @Column(name="status",nullable=false,length=50)
//    private String status;
//    
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "seat_id")
//    private Seat seat;
//
//    @Column(name="reserved_at",nullable=false)
//    private LocalDateTime reservedAt;
//    
//    @Column(name="created_at",nullable=false)
//    private LocalDateTime createdAt;
//    
//    @Column(name="updated_at",nullable=false)
//    private LocalDateTime updatedAt;
//
//	public Reservation(Long id, Long userId, Long preformanceId, String status, Seat seat,
//			LocalDateTime reservedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
//		super();
//		this.id = id;
//		this.userId = userId;
//		this.preformanceId = preformanceId;		
//		this.status = status;
//		this.seat = seat;
//		this.reservedAt = reservedAt;
//		this.createdAt = createdAt;
//		this.updatedAt = updatedAt;
//	}
//    
//    public Reservation(Long userId, Seat seat) {
//        this.userId = userId;
//        this.seat = seat;
//        this.reservedAt = LocalDateTime.now();
//    }
//}