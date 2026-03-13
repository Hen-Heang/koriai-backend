package com.heang.koriaibackend.domain.auth.controller;

import com.heang.koriaibackend.common.api.ApiResponse;
import com.heang.koriaibackend.domain.auth.dto.AuthResponse;
import com.heang.koriaibackend.domain.auth.dto.LoginRequest;
import com.heang.koriaibackend.domain.auth.dto.RegisterRequest;
import com.heang.koriaibackend.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.success(authService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.success(authService.login(req));
    }
}
