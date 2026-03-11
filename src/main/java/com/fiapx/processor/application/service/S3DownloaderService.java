package com.fiapx.processor.application.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.File;
import java.nio.file.Paths;

@Service
public class S3DownloaderService {

    private final S3Client s3Client;

    public S3DownloaderService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Faz o download de um arquivo do S3 para um diretório local temporário.
     * @param bucketName O nome do bucket.
     * @param key A chave do objeto no S3.
     * @return O arquivo local baixado.
     */
    public File downloadFile(String bucketName, String key) {
        File localFile = new File("/tmp/downloads/" + key);
        localFile.getParentFile().mkdirs();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.getObject(getObjectRequest, Paths.get(localFile.getAbsolutePath()));
        return localFile;
    }
}
