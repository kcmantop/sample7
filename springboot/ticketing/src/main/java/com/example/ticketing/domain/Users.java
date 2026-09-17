package com.example.ticketing.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Users(String email, String password, String name, UserRole role, LocalDateTime createdAt, LocalDateTime updatedAt) {
    	System.out.println("---41");
    	
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role != null ? role : UserRole.ROLE_USER;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 비즈니스 로직: 회원 정보 수정 등
    public void updateInfo(String name) {
        this.name = name;
    }
}