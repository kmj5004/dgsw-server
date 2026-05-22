package com.kmj5004.hdljudge.leaderboard;

import com.kmj5004.hdljudge.common.error.ApiException;
import com.kmj5004.hdljudge.common.error.ErrorCode;
import com.kmj5004.hdljudge.domain.challenge.Challenge;
import com.kmj5004.hdljudge.domain.challenge.ChallengeRepository;
import com.kmj5004.hdljudge.domain.leaderboard.LeaderboardEntry;
import com.kmj5004.hdljudge.domain.leaderboard.LeaderboardEntryRepository;
import com.kmj5004.hdljudge.leaderboard.dto.LeaderboardEntryView;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LeaderboardService {

    private final ChallengeRepository challenges;
    private final LeaderboardEntryRepository entries;

    public List<LeaderboardEntryView> topBySlug(String slug) {
        Challenge challenge = challenges.findBySlugAndDeletedAtIsNull(slug)
            .orElseThrow(() -> new ApiException(ErrorCode.CHALLENGE_NOT_FOUND));
        List<LeaderboardEntry> rows = entries.findTop50ByChallengeIdOrderByBestScoreDescBestGateCountAsc(challenge.getId());
        List<LeaderboardEntryView> result = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            result.add(LeaderboardEntryView.from(i + 1, rows.get(i)));
        }
        return result;
    }
}
