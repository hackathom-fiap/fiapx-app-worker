package com.fiapx.processor.application.usecase;

import com.fiapx.processor.domain.service.NotificationPort;
import com.fiapx.processor.domain.service.VideoApiPort;
import com.fiapx.processor.domain.service.VideoProcessingPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessVideoUseCaseTest {

    @Mock
    private VideoProcessingPort videoProcessing;

    @Mock
    private VideoApiPort videoApi;

    @Mock
    private NotificationPort notification;

    @InjectMocks
    private ProcessVideoUseCase processVideoUseCase;

    private UUID videoId;
    private String storagePath;
    private String userEmail;
    private String contentType;

    @BeforeEach
    void setUp() {
        videoId = UUID.randomUUID();
        storagePath = "/tmp/video.mp4";
        userEmail = "test@example.com";
        contentType = "video/mp4";
    }

    @Test
    void shouldProcessVideoSuccessfully() {
        // Given
        File imagesDir = new File("/tmp/images");
        File zipFile = new File("/tmp/videos/" + videoId + "/images.zip");
        
        when(videoProcessing.extractImages(videoId, storagePath)).thenReturn(imagesDir);
        when(videoProcessing.createZip(videoId, imagesDir)).thenReturn(zipFile);

        // When
        processVideoUseCase.execute(videoId, storagePath, userEmail, contentType);

        // Then
        verify(videoApi).updateStatus(videoId, "PROCESSING", null);
        verify(videoProcessing).extractImages(videoId, storagePath);
        verify(videoProcessing).createZip(videoId, imagesDir);
        verify(videoApi).updateStatus(videoId, "COMPLETED", zipFile.getAbsolutePath());
        verify(notification, never()).sendErrorNotification(any(), any(), any());
    }

    @Test
    void shouldThrowExceptionWhenContentTypeIsNotVideo() {
        // Given
        contentType = "image/jpeg";

        // When
        processVideoUseCase.execute(videoId, storagePath, userEmail, contentType);

        // Then
        verify(videoApi).updateStatus(videoId, "PROCESSING", null);
        verify(videoApi).updateStatus(videoId, "ERROR", null);
        verify(notification).sendErrorNotification(eq(userEmail), eq(videoId), contains("fraude de formato"));
        verify(videoProcessing, never()).extractImages(any(), any());
        verify(videoProcessing, never()).createZip(any(), any());
    }

    @Test
    void shouldHandleNullContentType() {
        // Given
        contentType = null;
        when(videoProcessing.extractImages(eq(videoId), eq(storagePath))).thenReturn(new File("/tmp/images"));
        when(videoProcessing.createZip(eq(videoId), any(File.class))).thenReturn(new File("/tmp/videos/" + videoId + "/images.zip"));

        // When
        processVideoUseCase.execute(videoId, storagePath, userEmail, contentType);

        // Then
        verify(videoApi).updateStatus(videoId, "PROCESSING", null);
        verify(videoProcessing).extractImages(videoId, storagePath);
        verify(videoProcessing).createZip(eq(videoId), any(File.class));
        verify(videoApi).updateStatus(eq(videoId), eq("COMPLETED"), anyString());
        verify(notification, never()).sendErrorNotification(any(), any(), any());
    }

    @Test
    void shouldHandleExceptionDuringProcessing() {
        // Given
        String errorMessage = "Processing failed";
        when(videoProcessing.extractImages(videoId, storagePath))
            .thenThrow(new RuntimeException(errorMessage));

        // When
        processVideoUseCase.execute(videoId, storagePath, userEmail, contentType);

        // Then
        verify(videoApi).updateStatus(videoId, "PROCESSING", null);
        verify(videoProcessing).extractImages(videoId, storagePath);
        verify(videoApi).updateStatus(videoId, "ERROR", null);
        verify(notification).sendErrorNotification(userEmail, videoId, errorMessage);
        verify(videoProcessing, never()).createZip(any(), any());
    }

    @Test
    void shouldHandleExceptionDuringZipCreation() {
        // Given
        File imagesDir = new File("/tmp/images");
        String errorMessage = "ZIP creation failed";
        
        when(videoProcessing.extractImages(videoId, storagePath)).thenReturn(imagesDir);
        when(videoProcessing.createZip(videoId, imagesDir))
            .thenThrow(new RuntimeException(errorMessage));

        // When
        processVideoUseCase.execute(videoId, storagePath, userEmail, contentType);

        // Then
        verify(videoApi).updateStatus(videoId, "PROCESSING", null);
        verify(videoProcessing).extractImages(videoId, storagePath);
        verify(videoProcessing).createZip(videoId, imagesDir);
        verify(videoApi).updateStatus(videoId, "ERROR", null);
        verify(notification).sendErrorNotification(userEmail, videoId, errorMessage);
    }

    @Test
    void shouldNotSendNotificationWhenUserEmailIsBlank() {
        // Given
        userEmail = " ";
        when(videoProcessing.extractImages(videoId, storagePath))
            .thenThrow(new RuntimeException("Processing failed"));

        // When
        processVideoUseCase.execute(videoId, storagePath, userEmail, contentType);

        // Then
        verify(videoApi).updateStatus(videoId, "PROCESSING", null);
        verify(videoApi).updateStatus(videoId, "ERROR", null);
        verify(notification, never()).sendErrorNotification(any(), any(), any());
    }

    @Test
    void shouldNotSendNotificationWhenUserEmailIsNull() {
        // Given
        userEmail = null;
        when(videoProcessing.extractImages(videoId, storagePath))
            .thenThrow(new RuntimeException("Processing failed"));

        // When
        processVideoUseCase.execute(videoId, storagePath, userEmail, contentType);

        // Then
        verify(videoApi).updateStatus(videoId, "PROCESSING", null);
        verify(videoApi).updateStatus(videoId, "ERROR", null);
        verify(notification, never()).sendErrorNotification(any(), any(), any());
    }
}
