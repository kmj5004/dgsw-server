package com.kmj5004.hdljudge.playground;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hdljudge.synthesis")
public record SynthesisProperties(
    long cacheTtlSeconds,
    int maxGateCount
) {
}
