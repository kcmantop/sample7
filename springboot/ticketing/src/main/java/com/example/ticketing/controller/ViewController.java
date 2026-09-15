package com.example.ticketing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view")
public class ViewController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @GetMapping("/queue/{performanceId}")
    public String queuePage(@PathVariable Long performanceId, Model model) {
        model.addAttribute("performanceId", performanceId);
        return "queue";
    }

    @GetMapping("/reservation/{performanceId}")
    public String reservationPage(@PathVariable Long performanceId, Model model) {
        model.addAttribute("performanceId", performanceId);
        return "reservation";
    }
}