package com.vinod.bookservicegateway;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BookServiceGatewayApplicationTests {

    @Autowired
    private Environment environment;

    @Test
    void contextLoads() {
        assertThat(environment).isNotNull();
    }

    @Test
    void gatewayRoutesAreConfigured() {
        Map<String, Object> properties = Map.of(
                "spring.cloud.gateway.routes[0].id", "user-service",
                "spring.cloud.gateway.routes[0].uri", "http://localhost:8081",
                "spring.cloud.gateway.routes[0].predicates[0]", "Path=/api/user/**",
                "spring.cloud.gateway.routes[1].id", "inventory-service",
                "spring.cloud.gateway.routes[1].uri", "http://localhost:8082",
                "spring.cloud.gateway.routes[1].predicates[0]", "Path=/api/inventory/**"
        );

        properties.forEach((key, expectedValue) ->
                assertThat(environment.getProperty(key)).isEqualTo(expectedValue));
    }
}
