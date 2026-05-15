package com.kmj5004.hdljudge.domain.leaderboard;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderboardEntryRepository extends JpaRepository<LeaderboardEntry, Long> {

    Optional<LeaderboardEntry> findByChallengeIdAndUserId(Long challengeId, Long userId);

    List<LeaderboardEntry> findTop50ByChallengeIdOrderByBestScoreDescBestGateCountAsc(Long challengeId);

    long countByUserIdAndBestScore(Long userId, int bestScore);

    List<LeaderboardEntry> findByUserId(Long userId);
}
