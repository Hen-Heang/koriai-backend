package com.heang.koriaibackend.domain.auth.service;

import com.heang.koriaibackend.common.api.Code;
import com.heang.koriaibackend.common.exception.BusinessException;
import com.heang.koriaibackend.domain.auth.dto.AuthResponse;
import com.heang.koriaibackend.domain.auth.dto.LoginRequest;
import com.heang.koriaibackend.domain.auth.dto.RegisterRequest;
import com.heang.koriaibackend.domain.users.mapper.UserMapper;
import com.heang.koriaibackend.domain.users.model.User;
import com.heang.koriaibackend.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${openai.model:gpt-5-mini}")
    private String defaultPreferredModel;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        userMapper.findByEmail(email).ifPresent(existing -> {
            throw new BusinessException(Code.EMAIL_ALREADY_EXISTS);
        });

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(req.password()))
                .displayName(req.displayName().trim())
                .koreanLevel(req.koreanLevel().trim().toUpperCase())
                .preferredModel(defaultPreferredModel)
                .build();
        userMapper.insert(user);
        User savedUser = userMapper.findById(user.getId()).orElseThrow(() -> new BusinessException(Code.REGISTRATION_FAILED));
        String accessToken = jwtTokenProvider.createAccessToken(savedUser.getId(), savedUser.getEmail());
        return AuthResponse.of(accessToken, savedUser);
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        User user = userMapper.findByEmail(email).orElseThrow(() -> new BusinessException(Code.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BusinessException(Code.INVALID_CREDENTIALS);
        }
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        return AuthResponse.of(accessToken, user);
    }
}
