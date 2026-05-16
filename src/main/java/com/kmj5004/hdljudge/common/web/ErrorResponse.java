package com.kmj5004.hdljudge.common.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    String code,
    String message,
    String path,
    OffsetDateTime timestamp,
    List<FieldViolation> violations
) {
    public record FieldViolation(String field, String reason) {}
}
