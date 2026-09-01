package com.zestindia.productapi.service.impl;

import com.zestindia.productapi.dto.AuthResponse;
import com.zestindia.productapi.dto.LoginRequest;
import com.zestindia.productapi.dto.RegisterRequest;
import com.zestindia.productapi.entity.AppUser;
import com.zestindia.productapi.entity.RefreshToken;
import com.zestindia.productapi.exception.DuplicateResourceException;
import com.zestindia.productapi.exception.InvalidTokenException;
import com.zestindia.productapi.exception.ResourceNotFoundException;
import com.zestindia.productapi.repository.AppUserRepository;
import com.zestindia.productapi.repository.RefreshTokenRepository;
import com.zestindia.productapi.security.JwtUtil;
import com.zestindia.productapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (appUserRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }

        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(AppUser.Role.ROLE_USER)
                .build();

        appUserRepository.save(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        AppUser user = appUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUsername()));

        UserDetails userDetails = User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .build();

        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshAccessToken(String refreshTokenValue) {
        RefreshToken existing = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (existing.isRevoked() || existing.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidTokenException("Refresh token is expired or has been revoked");
        }

        // Rotate: revoke the old token and issue a brand new one
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        AppUser user = existing.getUser();
        UserDetails userDetails = User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .build();

        String newAccessToken = jwtUtil.generateAccessToken(userDetails);
        String newRefreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private String createRefreshToken(AppUser user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }
}
