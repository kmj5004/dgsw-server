package com.kmj5004.hdljudge.auth.service;

import com.kmj5004.hdljudge.domain.auth.RefreshTokenRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;








@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupTask {

    private final RefreshTokenRepository refreshTokens;


    @Value("${hdljudge.refresh-token.cleanup-grace-seconds:0}")
    private long graceSeconds;

    @Scheduled(initialDelay = 3_600_000L, fixedDelay = 86_400_000L)
    @Transactional
    public void purgeExpired() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(graceSeconds);
        int removed = refreshTokens.deleteAllExpiredBefore(cutoff);
        if (removed > 0) {
            log.info("RefreshToken cleanup: removed {} expired tokens (cutoff={})", removed, cutoff);
        } else {
            log.debug("RefreshToken cleanup: nothing to remove (cutoff={})", cutoff);
        }
    }
}
