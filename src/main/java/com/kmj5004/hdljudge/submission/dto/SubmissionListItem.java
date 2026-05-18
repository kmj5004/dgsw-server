package com.kmj5004.hdljudge.submission.dto;

import com.kmj5004.hdljudge.common.enums.SubmissionStatus;
import com.kmj5004.hdljudge.domain.submission.Submission;
import java.time.LocalDateTime;

public record SubmissionListItem(
    Long id,
    String challengeSlug,
    SubmissionStatus status,
    int score,
    LocalDateTime submittedAt,
    LocalDateTime completedAt
) {

    public static SubmissionListItem from(Submission s) {
        return new SubmissionListItem(
            s.getId(),
            s.getChallenge().getSlug(),
            s.getStatus(),
            s.getScore(),
            s.getSubmittedAt(),
            s.getCompletedAt()
        );
    }
}
