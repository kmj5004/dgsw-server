package com.kmj5004.hdljudge.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hdljudge.jwt")
public record JwtProperties(
    String issuer,
    String secret,
    long accessTtlSeconds,
    long refreshTtlSeconds
) {
}
