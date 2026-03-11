package com.fiapx.processor.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3DownloaderServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3DownloaderService s3DownloaderService;

    @TempDir
    Path tempDir;

    @Test
    void shouldDownloadFileSuccessfully() {
        // Given
        String bucketName = "test-bucket";
        String key = "uploads/test-video.mp4";
        
        // Mock the S3 client call
        // Note: software.amazon.awssdk v2 has a complex way to mock getObject with Paths
        // We will just verify the interaction.
        
        // When
        File downloadedFile = s3DownloaderService.downloadFile(bucketName, key);

        // Then
        assertNotNull(downloadedFile);
        // Verifica se o caminho do arquivo baixado contém o nome esperado
        assertTrue(downloadedFile.getPath().contains(key));
        
        verify(s3Client).getObject(any(GetObjectRequest.class), any(Path.class));
    }
}
