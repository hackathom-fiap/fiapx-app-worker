package com.fiapx.processor.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    @Bean
    public S3Client s3Client() {
        // O SDK da AWS usará automaticamente as credenciais do Service Account do pod (IRSA)
        return S3Client.builder()
                .region(Region.US_EAST_1) // Você pode externalizar isso para o application.yml se desejar
                .build();
    }
}
