package com.example.ticketing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ticketing.domain.Users;
import com.example.ticketing.dto.ReservationDto;
import com.example.ticketing.facade.RedissonLockReservationFacade;
import com.example.ticketing.repository.UsersRepository;
import com.example.ticketing.service.OptimisticReservationService;
import com.example.ticketing.service.PessimisticReservationService;
import com.example.ticketing.service.ReservationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    
    // 1.비관적
    private final PessimisticReservationService pessimisticService;
    // 2.낙관적
    private final OptimisticReservationService optimisticService;
    // 3.Redis
    private final RedissonLockReservationFacade redissonFacade;
    
    private final UsersRepository usersRepository;

    @PostMapping
    public ResponseEntity<ReservationDto.Response> reserveSeat(
            //@AuthenticationPrincipal Long userId,  // jmeter 테스트를 위해 코멘트 처리, 테스트 후 코멘트 제거 처리 
            @RequestBody ReservationDto.CreateRequest request) {
    	
    	System.out.println("---82");

    	// jmeter테스트 때문에 코멘트 처리 
        ReservationDto.Response response = reservationService.reserveSeat(request.getUserId(), request);
        
    	// 유저가 존재하지 않을 경우 EntityNotFoundException 발생
    	Users user = usersRepository.findById(request.getUserId())
    	        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + request.getUserId()));
    	
    	//------------------------------------------------------------------
    	// JMETER 테스트 
    	//------------------------------------------------------------------
    	// 1.비관적
    	//ReservationDto.Response response = pessimisticService.reserve(request.getSeatId(), user);
    	
    	System.out.println("---83");
        
        // 2.낙관적 
    	//ReservationDto.Response response = optimisticService.reserve(request.getUserId(), user);
                
        // 3.redis 
    	//ReservationDto.Response response = redissonFacade.reserve(request.getUserId(), user);     
    	//------------------------------------------------------------------
        
        return ResponseEntity.ok(response);
    }
}