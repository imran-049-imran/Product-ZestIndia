package com.zestindia.productapi.service;

import com.zestindia.productapi.dto.AuthResponse;
import com.zestindia.productapi.dto.LoginRequest;
import com.zestindia.productapi.dto.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshAccessToken(String refreshToken);

    void logout(String refreshToken);
}
