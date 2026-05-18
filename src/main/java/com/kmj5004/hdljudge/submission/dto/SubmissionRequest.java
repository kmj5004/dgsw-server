package com.kmj5004.hdljudge.submission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmissionRequest(
    @NotBlank @Size(max = 120) String challengeSlug,
    @NotBlank @Size(max = 65535) String code
) {
}
