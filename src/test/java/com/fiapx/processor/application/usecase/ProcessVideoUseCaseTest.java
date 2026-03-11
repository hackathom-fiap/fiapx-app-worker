package com.fiapx.processor.application.usecase;

import com.fiapx.processor.application.service.S3DownloaderService;
import com.fiapx.processor.application.service.S3UploaderService;
import com.fiapx.processor.domain.service.NotificationPort;
import com.fiapx.processor.domain.service.VideoApiPort;
import com.fiapx.processor.domain.service.VideoProcessingPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessVideoUseCaseTest {

    @Mock
    private VideoProcessingPort videoProcessing;
    @Mock
    private VideoApiPort videoApi;
    @Mock
    private NotificationPort notification;
    @Mock
    private S3UploaderService s3Uploader;
    @Mock
    private S3DownloaderService s3Downloader;

    @InjectMocks
    private ProcessVideoUseCase processVideoUseCase;

    private final String testBucketName = "test-bucket";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(processVideoUseCase, "s3BucketName", testBucketName);
    }

    @Test
    void shouldProcessVideoAndUploadToS3Successfully() throws Exception {
        // Given
        UUID videoId = UUID.randomUUID();
        String s3Path = "s3://test-bucket/uploads/" + videoId + ".mp4";
        String s3Key = "uploads/" + videoId + ".mp4";
        String userEmail = "test@example.com";
        String contentType = "video/mp4";

        File mockDownloadedFile = new File("/tmp/" + videoId + ".mp4");
        File mockImagesDir = new File("/tmp/images");
        File mockZipFile = new File("/tmp/video.zip");

        when(s3Downloader.downloadFile(testBucketName, s3Key)).thenReturn(mockDownloadedFile);
        when(videoProcessing.extractImages(videoId, mockDownloadedFile.getAbsolutePath())).thenReturn(mockImagesDir);
        when(videoProcessing.createZip(videoId, mockImagesDir)).thenReturn(mockZipFile);

        ArgumentCaptor<String> s3UrlCaptor = ArgumentCaptor.forClass(String.class);

        // When
        processVideoUseCase.execute(videoId, s3Path, userEmail, contentType);

        // Then
        verify(s3Uploader).uploadFile(eq(testBucketName), eq("processed/" + videoId + ".zip"), eq(mockZipFile.getAbsolutePath()));
        verify(videoApi).updateStatus(eq(videoId), eq("COMPLETED"), s3UrlCaptor.capture());
        String expectedS3Url = "https://" + testBucketName + ".s3.amazonaws.com/processed/" + videoId + ".zip";
        assertEquals(expectedS3Url, s3UrlCaptor.getValue());
        verify(notification, never()).sendErrorNotification(any(), any(), any());
    }

    @Test
    void shouldHandleProcessingErrorAndNotify() {
        // Given
        UUID videoId = UUID.randomUUID();
        String s3Path = "s3://test-bucket/uploads/" + videoId + ".mp4";
        String s3Key = "uploads/" + videoId + ".mp4";
        String userEmail = "test@example.com";
        String contentType = "video/mp4";
        RuntimeException testException = new RuntimeException("Test S3 download error");

        when(s3Downloader.downloadFile(testBucketName, s3Key)).thenThrow(testException);

        // When
        processVideoUseCase.execute(videoId, s3Path, userEmail, contentType);

        // Then
        verify(videoApi).updateStatus(eq(videoId), eq("ERROR"), eq(null));
        verify(notification).sendErrorNotification(eq(userEmail), eq(videoId), eq(testException.getMessage()));
        verify(s3Uploader, never()).uploadFile(any(), any(), any());
    }
}
