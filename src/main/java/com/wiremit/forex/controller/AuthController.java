package com.wiremit.forex.controller;

import com.wiremit.forex.dto.request.LoginRequest;
import com.wiremit.forex.dto.request.RegisterRequest;
import com.wiremit.forex.dto.response.AuthResponse;
import com.wiremit.forex.service.AuthService;
import com.wiremit.forex.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with the provided registration details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid registration data",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Parameter(description = "Registration request containing user details")
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        return authService.register(request, httpRequest);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates a user with email/username and password")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request format")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> authenticate(
            @Parameter(description = "Login credentials")
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        return authService.authenticate(request, httpRequest);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generates a new access token using a valid refresh token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Parameter(description = "Authorization header with Bearer token", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest) {
        return authService.refreshToken(authHeader, httpRequest);
    }

}