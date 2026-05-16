package com.kmj5004.hdljudge.security;

import com.kmj5004.hdljudge.common.enums.Role;
import com.kmj5004.hdljudge.common.error.ApiException;
import com.kmj5004.hdljudge.common.error.ErrorCode;
import com.kmj5004.hdljudge.domain.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String CLAIM_TYPE = "typ";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_EMAIL = "email";


    private static final String DEFAULT_SECRET_FALLBACK =
        "change-me-please-use-a-long-random-secret-at-least-32-bytes";

    private final SecretKey signingKey;
    private final String issuer;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public JwtTokenProvider(JwtProperties props) {
        byte[] secretBytes = props.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("hdljudge.jwt.secret must be at least 32 bytes for HS256");
        }
        if (DEFAULT_SECRET_FALLBACK.equals(props.secret())) {
            log.warn("=================================================================");
            log.warn(" hdljudge.jwt.secret 가 application.yml 의 기본 fallback 값입니다.");
            log.warn(" 운영 배포 전에 반드시 JWT_SECRET 환경변수를 32바이트 이상의");
            log.warn(" 랜덤 문자열로 설정하세요. 그렇지 않으면 모든 토큰이 누구에게나");
            log.warn(" 위·변조됩니다.");
            log.warn("=================================================================");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
        this.issuer = props.issuer();
        this.accessTtlSeconds = props.accessTtlSeconds();
        this.refreshTtlSeconds = props.refreshTtlSeconds();
    }

    public IssuedTokens issue(User user) {
        Instant now = Instant.now();
        Instant accessExp = now.plusSeconds(accessTtlSeconds);
        Instant refreshExp = now.plusSeconds(refreshTtlSeconds);
        String access = build(user, TokenType.ACCESS, now, accessExp);
        String refresh = build(user, TokenType.REFRESH, now, refreshExp);
        return new IssuedTokens(access, refresh, toLocalDateTime(accessExp), toLocalDateTime(refreshExp));
    }

    public AuthPrincipal parseAccess(String token) {
        Claims claims = parse(token, TokenType.ACCESS);
        return toPrincipal(claims);
    }

    public Claims parseRefresh(String token) {
        return parse(token, TokenType.REFRESH);
    }

    public String hashRefreshToken(String token) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public long accessTtlSeconds() {
        return accessTtlSeconds;
    }

    private String build(User user, TokenType type, Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .issuer(issuer)
            .subject(String.valueOf(user.getId()))
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiresAt))
            .claim(CLAIM_TYPE, type.name())
            .claim(CLAIM_ROLE, user.getRole().name())
            .claim(CLAIM_EMAIL, user.getEmail())
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact();
    }

    private Claims parse(String token, TokenType expectedType) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            String typ = claims.get(CLAIM_TYPE, String.class);
            if (typ == null || !typ.equals(expectedType.name())) {
                throw new ApiException(ErrorCode.INVALID_TOKEN, "Token type mismatch");
            }
            return claims;
        } catch (ExpiredJwtException e) {
            throw new ApiException(ErrorCode.EXPIRED_TOKEN, "Token expired", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(ErrorCode.INVALID_TOKEN, "Invalid token", e);
        }
    }

    private AuthPrincipal toPrincipal(Claims claims) {
        Long userId = Long.valueOf(claims.getSubject());
        String email = claims.get(CLAIM_EMAIL, String.class);
        Role role = Role.valueOf(claims.get(CLAIM_ROLE, String.class));
        return new AuthPrincipal(userId, email, role);
    }

    private static LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
