package com.fiapx.processor.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class AwsConfig {

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }
}
