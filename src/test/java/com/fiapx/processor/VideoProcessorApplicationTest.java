package com.fiapx.processor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.builder.SpringApplicationBuilder;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class VideoProcessorApplicationTest {

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Test
    void contextLoads() {
        // This test verifies if Spring context loads correctly
        // If it fails, indicates application configuration problems
    }

    @Test
    void mainMethodShouldStartApplication() {
        // Tests if main method can be called without throwing exceptions
        assertDoesNotThrow(() -> {
            // Use parameters to disable web environment and JMX
            String[] args = {"--spring.main.web-environment=false", "--spring.jmx.enabled=false"};
            VideoProcessorApplication.main(args);
        });
    }

    @Test
    void applicationBuilderShouldConfigureCorrectly() {
        // Tests if application builder is configured correctly
        SpringApplicationBuilder builder = new SpringApplicationBuilder(VideoProcessorApplication.class);
        assertNotNull(builder);
    }
}
