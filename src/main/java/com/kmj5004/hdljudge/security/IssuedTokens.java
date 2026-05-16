package com.kmj5004.hdljudge.security;

import java.time.LocalDateTime;

public record IssuedTokens(
    String accessToken,
    String refreshToken,
    LocalDateTime accessExpiresAt,
    LocalDateTime refreshExpiresAt
) {
}
