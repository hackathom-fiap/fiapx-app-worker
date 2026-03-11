package com.fiapx.processor.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.ses.SesClient;

import static org.assertj.core.api.Assertions.assertThat;

class AwsConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void awsBeansShouldBeCreated() {
        this.contextRunner.withUserConfiguration(AwsConfig.class)
                .run(context -> {
                    // Verifica se os beans S3Client e SesClient existem no contexto
                    assertThat(context).hasSingleBean(S3Client.class);
                    assertThat(context).hasSingleBean(SesClient.class);
                    
                    assertThat(context.getBean(S3Client.class)).isNotNull();
                    assertThat(context.getBean(SesClient.class)).isNotNull();
                });
    }
}
