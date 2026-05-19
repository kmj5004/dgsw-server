package com.kmj5004.hdljudge.judge.adapter;

public record ResourceLimits(
    long timeLimitNs,
    int wallTimeLimitMs,
    int memoryLimitMb
) {
}
