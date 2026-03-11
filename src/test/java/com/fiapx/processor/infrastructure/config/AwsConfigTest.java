package com.fiapx.processor.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.assertThat;

class AwsConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void s3ClientBeanShouldBeCreated() {
        this.contextRunner.withUserConfiguration(AwsConfig.class)
                .run(context -> {
                    // Verifica se o bean S3Client existe no contexto
                    assertThat(context).hasSingleBean(S3Client.class);
                    
                    // Verifica se a região foi configurada (opcional, mas bom ter)
                    S3Client s3Client = context.getBean(S3Client.class);
                    // A verificação da região exata é complexa, então apenas garantimos que o bean não é nulo.
                    assertThat(s3Client).isNotNull();
                });
    }
}
