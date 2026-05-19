package com.kmj5004.hdljudge.judge;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hdljudge.judge")
public record JudgeProperties(
    int workerPoolSize,
    int queueCapacity,
    Docker docker,
    Limits limits
) {

    public record Docker(
        String socket,
        String workerImage,
        String network,
        boolean readOnlyRoot,
        int cpuQuotaPercent,
        int memoryLimitMb,
        int pidsLimit
    ) {}

    public record Limits(
        long defaultTimeLimitNs,
        int defaultWallTimeLimitMs,
        int defaultMemoryLimitMb
    ) {}
}
