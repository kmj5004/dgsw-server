package com.kmj5004.hdljudge.judge;

import com.kmj5004.hdljudge.common.enums.Language;
import com.kmj5004.hdljudge.judge.adapter.ResourceLimits;
import java.util.List;

public record JobContext(
    Long submissionId,
    Long challengeId,
    Long userId,
    Language language,
    String userCode,
    String testbench,
    ResourceLimits limits,
    List<TestVectorRef> vectors
) {

    public record TestVectorRef(Long id, int ordering, String expectedJson, int weight) {
    }
}
