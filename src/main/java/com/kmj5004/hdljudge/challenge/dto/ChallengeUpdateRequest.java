package com.kmj5004.hdljudge.challenge.dto;

import com.kmj5004.hdljudge.common.enums.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;




public record ChallengeUpdateRequest(
    @NotBlank @Size(max = 255) String title,
    @NotBlank String description,
    @NotBlank String skeleton,
    @NotBlank String hiddenTestbench,
    @Positive long timeLimitNs,
    @Positive int wallTimeLimitMs,
    @Positive int memoryLimitMb,
    @NotNull Difficulty difficulty,
    Set<@Size(max = 40) String> tags,
    @Valid List<TestVectorRequest> testVectors
) {
}
