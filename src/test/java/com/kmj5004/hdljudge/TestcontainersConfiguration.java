package com.kmj5004.hdljudge;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	MySQLContainer mysqlContainer() {

		return (MySQLContainer) new MySQLContainer(DockerImageName.parse("mysql:8.0"))
			.withDatabaseName("hdljudge")
			.withUsername("hdljudge")
			.withPassword("hdljudge")
			.withUrlParam("useSSL", "false")
			.withUrlParam("allowPublicKeyRetrieval", "true")
			.withUrlParam("serverTimezone", "UTC")
			.withUrlParam("characterEncoding", "UTF-8");
	}

	@Bean
	@ServiceConnection(name = "redis")
	GenericContainer<?> redisContainer() {
		return new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);
	}

}
