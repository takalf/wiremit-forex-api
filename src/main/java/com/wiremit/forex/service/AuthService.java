package com.wiremit.forex.service;

import com.wiremit.forex.dto.request.LoginRequest;
import com.wiremit.forex.dto.request.RegisterRequest;
import com.wiremit.forex.dto.response.AuthResponse;
import com.wiremit.forex.model.User;
import com.wiremit.forex.repository.UserRepository;
import com.wiremit.forex.security.JwtService;
import com.wiremit.forex.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public ResponseEntity<ApiResponse<AuthResponse>> register(RegisterRequest request, HttpServletRequest httpRequest) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        AuthResponse authResponse = new AuthResponse(accessToken, refreshToken);

        ApiResponse<AuthResponse> response = ApiResponse
                .success("User registered successfully", authResponse)
                .path(httpRequest.getRequestURI())
                .status(HttpStatus.CREATED.value());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public ResponseEntity<ApiResponse<AuthResponse>> authenticate(LoginRequest request, HttpServletRequest httpRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        AuthResponse authResponse = new AuthResponse(accessToken, refreshToken);

        ApiResponse<AuthResponse> response = ApiResponse
                .success("Login successful", authResponse)
                .path(httpRequest.getRequestURI());

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(String authHeader, HttpServletRequest httpRequest) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }

        String refreshToken = authHeader.substring(7);
        final String username = jwtService.extractUsername(refreshToken);

        if (username != null) {
            var user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Incorrect credentials"));

            if (jwtService.isTokenValid(refreshToken, user)) {
                var newAccessToken = jwtService.generateToken(user);
                AuthResponse authResponse = new AuthResponse(newAccessToken, refreshToken);

                ApiResponse<AuthResponse> response = ApiResponse
                        .success("Token refreshed successfully", authResponse)
                        .path(httpRequest.getRequestURI());

                return ResponseEntity.ok(response);
            }
        }

        throw new RuntimeException("Invalid refresh token");
    }
}