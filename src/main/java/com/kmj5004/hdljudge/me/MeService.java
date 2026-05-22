package com.kmj5004.hdljudge.me;

import com.kmj5004.hdljudge.domain.leaderboard.LeaderboardEntryRepository;
import com.kmj5004.hdljudge.domain.submission.Submission;
import com.kmj5004.hdljudge.domain.submission.SubmissionRepository;
import com.kmj5004.hdljudge.learningpath.LearningPathService;
import com.kmj5004.hdljudge.me.dto.UserProgress;
import com.kmj5004.hdljudge.submission.dto.SubmissionListItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MeService {

    private static final int PERFECT_SCORE = 100;

    private final SubmissionRepository submissions;
    private final LeaderboardEntryRepository leaderboard;
    private final LearningPathService learningPathService;

    public UserProgress getProgress(Long userId) {
        long totalSubmissions = submissions.countByUserId(userId);
        long attemptedChallenges = submissions.countDistinctChallengeIdByUserId(userId);
        long solvedChallenges = leaderboard.countByUserIdAndBestScore(userId, PERFECT_SCORE);
        List<Submission> recent = submissions.findTop5ByUserIdOrderBySubmittedAtDesc(userId);

        return new UserProgress(
            totalSubmissions,
            attemptedChallenges,
            solvedChallenges,
            recent.stream().map(SubmissionListItem::from).toList(),
            learningPathService.computeProgressFor(userId)
        );
    }
}
