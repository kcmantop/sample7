package com.example.ticketing.facade;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import com.example.ticketing.domain.Users;
import com.example.ticketing.service.OptimisticReservationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OptimisticLockReservationFacade {

    private final OptimisticReservationService optimisticReservationService;

    public void reserve(Long seatId, Users user) throws InterruptedException {
    	System.out.println("---71");
    	
        while (true) {
            try {
            	System.out.println("---72");
            	
                // 트랜잭션이 적용된 서비스 메서드 호출
                optimisticReservationService.reserve(seatId, user);
                
                System.out.println("---73");
                
                break; // 성공 시 루프 탈출
            } catch (OptimisticLockingFailureException e) {
                // 충돌 발생 시 50ms 대기 후 재시도
                Thread.sleep(50);
            }
        }
    }
}
