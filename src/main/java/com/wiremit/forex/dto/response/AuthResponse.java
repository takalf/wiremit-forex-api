package com.wiremit.forex.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private final String tokenType = "Bearer";
}
