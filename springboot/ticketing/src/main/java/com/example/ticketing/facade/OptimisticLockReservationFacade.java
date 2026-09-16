// 충돌 시 재시도를 담당하는 Facade
package com.example.ticketing.facade;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import com.example.ticketing.domain.Users;
import com.example.ticketing.service.OptimisticReservationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OptimisticLockReservationFacade {

    private final OptimisticReservationService reservationService;

    public void reserveSeat(Long seatId, Users user) throws InterruptedException {
        while (true) {
            try {
                reservationService.reserveSeat(seatId, user);
                break; // 성공 시 루프 탈출
            } catch (ObjectOptimisticLockingFailureException e) {
                // 충돌 발생 시 50ms 대기 후 재시도 (트래픽 폭주 시 DB 과부하 원인)
                Thread.sleep(50);
            }
        }
    }
}