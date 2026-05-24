package com.kmj5004.hdljudge.learningpath;

import com.kmj5004.hdljudge.common.error.ApiException;
import com.kmj5004.hdljudge.common.error.ErrorCode;
import com.kmj5004.hdljudge.domain.challenge.Challenge;
import com.kmj5004.hdljudge.domain.challenge.ChallengeRepository;
import com.kmj5004.hdljudge.domain.leaderboard.LeaderboardEntry;
import com.kmj5004.hdljudge.domain.leaderboard.LeaderboardEntryRepository;
import com.kmj5004.hdljudge.learningpath.dto.LearningPath;
import com.kmj5004.hdljudge.learningpath.dto.LearningPathDetail;
import com.kmj5004.hdljudge.learningpath.dto.LearningPathStep;
import com.kmj5004.hdljudge.learningpath.dto.LearningPathSummary;
import com.kmj5004.hdljudge.learningpath.dto.PathProgress;
import com.kmj5004.hdljudge.learningpath.dto.StepProgress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LearningPathService {

    private static final int PERFECT_SCORE = 100;

    private final LearningPathRegistry registry;
    private final ChallengeRepository challenges;
    private final LeaderboardEntryRepository leaderboard;

    public List<LearningPathSummary> list() {
        return registry.all().stream().map(LearningPathSummary::from).toList();
    }





    public List<PathProgress> computeProgressFor(Long userId) {
        if (userId == null) {
            return List.of();
        }
        List<LearningPath> paths = registry.all();
        if (paths.isEmpty()) {
            return List.of();
        }
        Set<String> allSlugs = new HashSet<>();
        for (LearningPath p : paths) {
            for (LearningPathStep s : p.steps()) {
                allSlugs.add(s.challengeSlug());
            }
        }
        Map<String, Long> challengeIdBySlug = new HashMap<>();
        for (Challenge c : challenges.findBySlugIn(allSlugs)) {
            challengeIdBySlug.put(c.getSlug(), c.getId());
        }
        Map<Long, Integer> bestScoreByChallengeId = new HashMap<>();
        for (LeaderboardEntry e : leaderboard.findByUserId(userId)) {
            bestScoreByChallengeId.put(e.getChallenge().getId(), e.getBestScore());
        }
        List<PathProgress> result = new ArrayList<>(paths.size());
        for (LearningPath p : paths) {
            int solved = 0;
            for (LearningPathStep step : p.steps()) {
                Long cid = challengeIdBySlug.get(step.challengeSlug());
                if (cid != null) {
                    Integer best = bestScoreByChallengeId.get(cid);
                    if (best != null && best >= PERFECT_SCORE) {
                        solved++;
                    }
                }
            }
            result.add(new PathProgress(p.slug(), p.title(), p.language(), solved, p.steps().size()));
        }
        return result;
    }

    public LearningPathDetail getBySlug(String slug, Long userId) {
        LearningPath path = registry.findBySlug(slug);
        if (path == null) {
            throw new ApiException(ErrorCode.NOT_FOUND, "learning path not found: " + slug);
        }


        List<String> stepSlugs = path.steps().stream()
            .map(LearningPathStep::challengeSlug).toList();
        Map<String, Long> challengeIdBySlug = new HashMap<>();
        for (Challenge c : challenges.findBySlugIn(stepSlugs)) {
            challengeIdBySlug.put(c.getSlug(), c.getId());
        }


        Map<Long, Integer> bestScoreByChallengeId = new HashMap<>();
        if (userId != null) {
            for (LeaderboardEntry e : leaderboard.findByUserId(userId)) {
                bestScoreByChallengeId.put(e.getChallenge().getId(), e.getBestScore());
            }
        }

        int solved = 0;
        java.util.List<StepProgress> stepProgress = new java.util.ArrayList<>(path.steps().size());
        for (LearningPathStep step : path.steps()) {
            Long cid = challengeIdBySlug.get(step.challengeSlug());
            int best = (cid != null && bestScoreByChallengeId.containsKey(cid))
                ? bestScoreByChallengeId.get(cid) : 0;
            boolean attempted = cid != null && bestScoreByChallengeId.containsKey(cid);
            stepProgress.add(StepProgress.of(step, best, attempted));
            if (best >= PERFECT_SCORE) {
                solved++;
            }
        }

        return new LearningPathDetail(
            path.slug(), path.title(), path.description(), path.goal(),
            path.language(), path.difficulty(), path.estimatedHours(),
            path.steps().size(), solved, stepProgress
        );
    }
}
