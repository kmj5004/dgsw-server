package com.kmj5004.hdljudge.judge;

import com.kmj5004.hdljudge.common.enums.SubmissionStatus;
import com.kmj5004.hdljudge.common.error.ApiException;
import com.kmj5004.hdljudge.common.error.ErrorCode;
import com.kmj5004.hdljudge.domain.challenge.Challenge;
import com.kmj5004.hdljudge.domain.challenge.TestVector;
import com.kmj5004.hdljudge.domain.challenge.TestVectorRepository;
import com.kmj5004.hdljudge.domain.leaderboard.LeaderboardEntry;
import com.kmj5004.hdljudge.domain.leaderboard.LeaderboardEntryRepository;
import com.kmj5004.hdljudge.domain.submission.SimulationRun;
import com.kmj5004.hdljudge.domain.submission.SimulationRunRepository;
import com.kmj5004.hdljudge.domain.submission.Submission;
import com.kmj5004.hdljudge.domain.submission.SubmissionRepository;
import com.kmj5004.hdljudge.judge.adapter.ResourceLimits;
import com.kmj5004.hdljudge.judge.adapter.SimulationOutcome;
import com.kmj5004.hdljudge.judge.output.JsonComparator;
import com.kmj5004.hdljudge.judge.output.OutputParser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;










@Slf4j
@Component
@RequiredArgsConstructor
public class JudgePipeline {

    private final SubmissionRepository submissions;
    private final SimulationRunRepository simulationRuns;
    private final TestVectorRepository testVectors;
    private final LeaderboardEntryRepository leaderboard;
    private final HdlAdapterDispatcher dispatcher;
    private final OutputParser parser;
    private final JsonComparator comparator;

    @Transactional
    public JobContext claim(Long submissionId) {
        Submission sub = submissions.findById(submissionId)
            .orElseThrow(() -> new ApiException(ErrorCode.SUBMISSION_NOT_FOUND));
        sub.markRunning();

        Challenge challenge = sub.getChallenge();
        List<TestVector> tvs = testVectors.findByChallengeIdOrderByOrderingAsc(challenge.getId());

        ResourceLimits limits = new ResourceLimits(
            challenge.getTimeLimitNs(),
            challenge.getWallTimeLimitMs(),
            challenge.getMemoryLimitMb()
        );

        return new JobContext(
            sub.getId(),
            challenge.getId(),
            sub.getUser().getId(),
            challenge.getLanguage(),
            sub.getCode(),
            challenge.getHiddenTestbench(),
            limits,
            tvs.stream()
                .map(tv -> new JobContext.TestVectorRef(tv.getId(), tv.getOrdering(), tv.getExpectedJson(), tv.getWeight()))
                .toList()
        );
    }

    public SimulationOutcome execute(JobContext ctx) {
        return dispatcher.forLanguage(ctx.language())
            .simulate(ctx.userCode(), ctx.testbench(), ctx.limits());
    }

    @Transactional
    public void score(JobContext ctx, SimulationOutcome outcome) {
        Submission sub = submissions.findById(ctx.submissionId())
            .orElseThrow(() -> new ApiException(ErrorCode.SUBMISSION_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now();

        if (outcome.status() == SimulationOutcome.Status.TIMEOUT) {
            sub.fail(SubmissionStatus.TIMEOUT, now);
            return;
        }
        if (outcome.status() == SimulationOutcome.Status.ERROR) {
            sub.fail(SubmissionStatus.ERROR, now);
            return;
        }

        Map<Integer, String> actualByOrder = parser.parse(outcome.stdout());
        int totalWeight = ctx.vectors().stream().mapToInt(JobContext.TestVectorRef::weight).sum();
        int earnedWeight = 0;

        for (JobContext.TestVectorRef tv : ctx.vectors()) {
            String actual = actualByOrder.get(tv.ordering());
            boolean passed = actual != null && comparator.equalsJson(actual, tv.expectedJson());

            simulationRuns.save(SimulationRun.builder()
                .submission(sub)
                .testVector(testVectors.getReferenceById(tv.id()))
                .passed(passed)
                .actualOutputJson(actual)
                .wallTimeMs((int) outcome.wallTimeMs())
                .stderrTail(passed ? null : outcome.stderrTail())
                .build());

            if (passed) {
                earnedWeight += tv.weight();
            }
        }

        int score = totalWeight == 0 ? 0 : (earnedWeight * 100) / totalWeight;
        sub.complete(score, now);
        upsertLeaderboard(sub, score);
    }

    @Transactional
    public void markFailed(Long submissionId, SubmissionStatus status) {
        submissions.findById(submissionId).ifPresent(s -> s.fail(status, LocalDateTime.now()));
    }

    private void upsertLeaderboard(Submission sub, int score) {
        Long challengeId = sub.getChallenge().getId();
        Long userId = sub.getUser().getId();

        LeaderboardEntry entry = leaderboard
            .findByChallengeIdAndUserId(challengeId, userId)
            .orElseGet(() -> leaderboard.save(LeaderboardEntry.builder()
                .challenge(sub.getChallenge())
                .user(sub.getUser())
                .bestScore(0)
                .build()));
        entry.updateIfBetter(score, null, sub);
    }
}
