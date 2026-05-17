package com.kmj5004.hdljudge.challenge.dto;

import com.kmj5004.hdljudge.common.enums.Difficulty;
import com.kmj5004.hdljudge.common.enums.Language;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;

public record ChallengeCreateRequest(
    @NotBlank @Size(max = 120) @Pattern(regexp = "[a-z0-9][a-z0-9-]*", message = "slug은 소문자/숫자/하이픈만 가능합니다") String slug,
    @NotBlank @Size(max = 255) String title,
    @NotBlank String description,
    @NotNull Language language,
    @NotBlank String skeleton,
    @NotBlank String hiddenTestbench,
    @Positive long timeLimitNs,
    @Positive int wallTimeLimitMs,
    @Positive int memoryLimitMb,
    @NotNull Difficulty difficulty,
    Set<@Size(max = 40) String> tags,
    @NotEmpty @Valid List<TestVectorRequest> testVectors
) {
}
