package com.kmj5004.hdljudge.leaderboard.dto;

import com.kmj5004.hdljudge.domain.leaderboard.LeaderboardEntry;

public record LeaderboardEntryView(
    int rank,
    String userEmail,
    int bestScore,
    Integer bestGateCount,
    Long bestSubmissionId
) {

    public static LeaderboardEntryView from(int rank, LeaderboardEntry e) {
        return new LeaderboardEntryView(
            rank,
            e.getUser().getEmail(),
            e.getBestScore(),
            e.getBestGateCount(),
            e.getBestSubmission() == null ? null : e.getBestSubmission().getId()
        );
    }
}
