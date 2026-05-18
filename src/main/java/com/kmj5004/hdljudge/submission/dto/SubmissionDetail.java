package com.kmj5004.hdljudge.submission.dto;

import com.kmj5004.hdljudge.common.enums.SubmissionStatus;
import com.kmj5004.hdljudge.domain.submission.SimulationRun;
import com.kmj5004.hdljudge.domain.submission.Submission;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record SubmissionDetail(
    Long id,
    Long userId,
    String challengeSlug,
    SubmissionStatus status,
    int score,
    LocalDateTime submittedAt,
    LocalDateTime completedAt,
    List<SimulationRunSummary> runs
) {

    public static SubmissionDetail from(Submission s, List<SimulationRun> runs) {
        return new SubmissionDetail(
            s.getId(),
            s.getUser().getId(),
            s.getChallenge().getSlug(),
            s.getStatus(),
            s.getScore(),
            s.getSubmittedAt(),
            s.getCompletedAt(),
            runs.stream()
                .sorted(Comparator.comparingInt(r -> r.getTestVector().getOrdering()))
                .map(SimulationRunSummary::from)
                .toList()
        );
    }
}
