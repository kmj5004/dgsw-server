package com.kmj5004.hdljudge.auth.service;

import com.kmj5004.hdljudge.auth.dto.LoginRequest;
import com.kmj5004.hdljudge.auth.dto.SignupRequest;
import com.kmj5004.hdljudge.auth.dto.TokenResponse;
import com.kmj5004.hdljudge.auth.dto.UserSummary;
import com.kmj5004.hdljudge.common.enums.Role;
import com.kmj5004.hdljudge.common.error.ApiException;
import com.kmj5004.hdljudge.common.error.ErrorCode;
import com.kmj5004.hdljudge.domain.auth.RefreshToken;
import com.kmj5004.hdljudge.domain.user.User;
import com.kmj5004.hdljudge.domain.user.UserRepository;
import com.kmj5004.hdljudge.security.IssuedTokens;
import com.kmj5004.hdljudge.security.JwtTokenProvider;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository users;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public UserSummary signup(SignupRequest req) {
        if (users.existsByEmail(req.email())) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        User user = users.save(User.builder()
            .email(req.email())
            .passwordHash(passwordEncoder.encode(req.password()))
            .role(Role.USER)
            .build());
        return UserSummary.from(user);
    }

    public TokenResponse login(LoginRequest req) {
        User user = users.findByEmail(req.email())
            .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }
        IssuedTokens issued = jwtProvider.issue(user);
        refreshTokenService.store(user, issued.refreshToken(), issued.refreshExpiresAt());
        return toTokenResponse(issued);
    }

    public TokenResponse refresh(String rawRefreshToken) {

        jwtProvider.parseRefresh(rawRefreshToken);

        RefreshToken consumed = refreshTokenService.consumeForRotation(rawRefreshToken);
        User user = consumed.getUser();

        IssuedTokens issued = jwtProvider.issue(user);
        refreshTokenService.store(user, issued.refreshToken(), issued.refreshExpiresAt());
        return toTokenResponse(issued);
    }

    public void logout(Long userId) {
        refreshTokenService.revokeAllForUser(userId);
    }

    private TokenResponse toTokenResponse(IssuedTokens t) {
        return new TokenResponse(
            t.accessToken(),
            t.refreshToken(),
            "Bearer",
            jwtProvider.accessTtlSeconds(),
            toOffset(t.accessExpiresAt()),
            toOffset(t.refreshExpiresAt())
        );
    }

    private static OffsetDateTime toOffset(java.time.LocalDateTime ldt) {
        return ldt.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }
}
