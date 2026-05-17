package com.kmj5004.hdljudge.challenge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record TestVectorRequest(
    @PositiveOrZero int ordering,
    @NotBlank @Size(max = 65535) String stimulusJson,
    @NotBlank @Size(max = 65535) String expectedJson,
    @Positive int weight
) {
}
