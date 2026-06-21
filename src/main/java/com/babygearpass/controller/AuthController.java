package com.babygearpass.controller;

import com.babygearpass.dto.auth.*;
import com.babygearpass.dto.common.ApiResponse;
import com.babygearpass.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserDTO> me() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(authService.getCurrentUser(username));
    }

    @PutMapping("/me")
    public ApiResponse<UserDTO> updateMe(@RequestBody @Valid UpdateUserRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success(authService.updateUser(username, request));
    }
}
