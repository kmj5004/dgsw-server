package com.kmj5004.hdljudge.auth.service;

import com.kmj5004.hdljudge.common.error.ApiException;
import com.kmj5004.hdljudge.common.error.ErrorCode;
import com.kmj5004.hdljudge.domain.auth.RefreshToken;
import com.kmj5004.hdljudge.domain.auth.RefreshTokenRepository;
import com.kmj5004.hdljudge.domain.user.User;
import com.kmj5004.hdljudge.security.JwtTokenProvider;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokens;
    private final RefreshTokenRevoker revoker;
    private final JwtTokenProvider jwtProvider;

    public void store(User user, String token, LocalDateTime expiresAt) {
        RefreshToken entity = RefreshToken.builder()
            .user(user)
            .tokenHash(jwtProvider.hashRefreshToken(token))
            .expiresAt(expiresAt)
            .build();
        refreshTokens.save(entity);
    }

    public RefreshToken consumeForRotation(String rawToken) {
        String hash = jwtProvider.hashRefreshToken(rawToken);
        RefreshToken stored = refreshTokens.findByTokenHash(hash)
            .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN, "Refresh token not registered"));

        if (stored.isRotated()) {


            Long userId = stored.getUser().getId();
            revoker.revokeAllForUser(userId);
            log.warn("Refresh token replay detected for userId={} — revoked all tokens", userId);
            throw new ApiException(ErrorCode.INVALID_TOKEN, "Refresh token already rotated");
        }
        if (stored.isExpired(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.EXPIRED_TOKEN, "Refresh token expired");
        }

        stored.rotate(LocalDateTime.now());
        return stored;
    }

    public void revokeAllForUser(Long userId) {
        refreshTokens.deleteAllByUserId(userId);
    }
}
