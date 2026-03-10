package com.fiapx.processor.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class S3UploaderServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3UploaderService s3UploaderService;

    @Test
    void shouldUploadFileSuccessfully() throws IOException {
        // Given
        String bucketName = "test-bucket";
        String key = "test-key";
        // Create a temporary file to upload
        Path tempFile = Files.createTempFile("test-upload", ".txt");
        Files.writeString(tempFile, "Hello, S3!");

        ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> requestBodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

        // When
        s3UploaderService.uploadFile(bucketName, key, tempFile.toString());

        // Then
        verify(s3Client).putObject(putObjectRequestCaptor.capture(), requestBodyCaptor.capture());

        PutObjectRequest capturedRequest = putObjectRequestCaptor.getValue();
        assertEquals(bucketName, capturedRequest.bucket());
        assertEquals(key, capturedRequest.key());

        // Clean up the temporary file
        Files.delete(tempFile);
    }
}
