package com.fiapx.processor.application.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;

@Service
public class S3UploaderService {

    private final S3Client s3Client;

    // S3Client é injetado automaticamente pelo Spring Cloud AWS
    public S3UploaderService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Faz o upload de um arquivo para um bucket S3.
     *
     * @param bucketName O nome do bucket.
     * @param key O nome do objeto (caminho do arquivo no bucket).
     * @param filePath O caminho local do arquivo a ser enviado.
     */
    public void uploadFile(String bucketName, String key, String filePath) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.putObject(objectRequest, RequestBody.fromFile(new File(filePath)));
    }
}
