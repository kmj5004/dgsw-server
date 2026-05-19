package com.kmj5004.hdljudge.judge;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(JudgeProperties.class)
@RequiredArgsConstructor
public class DockerClientConfig {

    @Bean(destroyMethod = "close")
    public DockerClient dockerClient(JudgeProperties props) {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost(props.docker().socket())
            .build();
        DockerHttpClient httpClient = new ZerodepDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .sslConfig(config.getSSLConfig())
            .maxConnections(Math.max(8, props.workerPoolSize() * 2))
            .build();
        return DockerClientImpl.getInstance(config, httpClient);
    }
}
