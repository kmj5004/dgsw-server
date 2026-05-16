package com.kmj5004.hdljudge.auth.dto;

import java.time.OffsetDateTime;

public record TokenResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long accessExpiresInSeconds,
    OffsetDateTime accessExpiresAt,
    OffsetDateTime refreshExpiresAt
) {
}
