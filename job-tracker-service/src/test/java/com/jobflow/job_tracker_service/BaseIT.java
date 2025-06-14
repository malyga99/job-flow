package com.jobflow.job_tracker_service;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@DirtiesContext(classMode = AFTER_CLASS)
@Testcontainers
public class BaseIT {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withUsername("test_username")
            .withPassword("test_password")
            .withDatabaseName("test_db");

    @Container
    private static final RedisContainer redis = new RedisContainer("redis/redis-stack:latest")
            .withExposedPorts(6379, 8001)
            .withEnv("REDIS_PASSWORD", "test_password");

    @Container
    private static final RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3-management")
            .withAdminUser("test_username")
            .withAdminPassword("test_password");


    @DynamicPropertySource
    private static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.username", postgres::getUsername);

        registry.add("spring.data.redis.port", redis::getRedisPort);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.username", () -> "default");
        registry.add("spring.data.redis.password", () -> "test_password");

        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
    }
}
