package com.springboot.api.controller;

import com.springboot.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/verify")
    public String verify(@RequestParam String token) {
        return authService.verifyAccount(token);
    }

    @PostMapping("/resend")
    public String resend(@RequestParam String email) {
        return authService.resendVerification(email);
    }
}