package com.kmj5004.hdljudge.submission.dto;

import com.kmj5004.hdljudge.common.enums.SubmissionStatus;

public record SubmissionAccepted(
    Long id,
    SubmissionStatus status
) {
}
