package com.fiapx.processor.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import static org.junit.jupiter.api.Assertions.*;

class RabbitMqConfigTest {

    private final RabbitMqConfig rabbitMqConfig = new RabbitMqConfig();

    @Test
    void shouldCreateMessageConverter() {
        // When
        Jackson2JsonMessageConverter converter = rabbitMqConfig.messageConverter();

        // Then
        assertNotNull(converter);
        assertInstanceOf(Jackson2JsonMessageConverter.class, converter);
    }

    @Test
    void shouldCreateRabbitListenerContainerFactory() {
        // Given
        org.springframework.amqp.rabbit.connection.ConnectionFactory mockConnectionFactory = 
            org.mockito.Mockito.mock(org.springframework.amqp.rabbit.connection.ConnectionFactory.class);

        // When
        SimpleRabbitListenerContainerFactory factory = rabbitMqConfig.rabbitListenerContainerFactory(mockConnectionFactory);

        // Then
        assertNotNull(factory);
        assertInstanceOf(SimpleRabbitListenerContainerFactory.class, factory);
    }

    @Test
    void shouldCreateFactoryWithMockConnection() {
        // Given
        org.springframework.amqp.rabbit.connection.ConnectionFactory mockConnectionFactory = 
            org.mockito.Mockito.mock(org.springframework.amqp.rabbit.connection.ConnectionFactory.class);

        // When
        SimpleRabbitListenerContainerFactory factory = rabbitMqConfig.rabbitListenerContainerFactory(mockConnectionFactory);

        // Then
        assertNotNull(factory);
        assertInstanceOf(SimpleRabbitListenerContainerFactory.class, factory);
        // Factory should be properly configured with the mock connection factory
        assertNotNull(factory);
    }
}
