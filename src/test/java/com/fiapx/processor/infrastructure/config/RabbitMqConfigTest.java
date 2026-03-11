package com.fiapx.processor.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RabbitMqConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void rabbitMqBeansShouldBeCreated() {
        // Mock a connection factory to avoid real connection attempts
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);

        this.contextRunner.withUserConfiguration(RabbitMqConfig.class)
                .withBean(ConnectionFactory.class, () -> connectionFactory)
                .run(context -> {
                    // Verifica se o Jackson2JsonMessageConverter foi criado
                    assertThat(context).hasBean("messageConverter");
                    
                    // Verifica se a Factory de listener foi criada
                    assertThat(context).hasSingleBean(SimpleRabbitListenerContainerFactory.class);
                });
    }
}
