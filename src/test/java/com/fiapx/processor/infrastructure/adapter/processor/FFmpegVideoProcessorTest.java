package com.fiapx.processor.infrastructure.adapter.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FFmpegVideoProcessorTest {

    private FFmpegVideoProcessor videoProcessor;
    private UUID videoId;
    private String storagePath;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        videoProcessor = new FFmpegVideoProcessor();
        videoId = UUID.randomUUID();
        storagePath = tempDir.resolve("test-video.mp4").toString();
    }

    @Test
    void shouldExtractImagesAndCreateDirectory() {
        // When
        File result = videoProcessor.extractImages(videoId, storagePath);

        // Then
        assertNotNull(result);
        assertTrue(result.exists());
        assertTrue(result.isDirectory());
        assertTrue(result.getPath().contains(videoId.toString()));
        assertTrue(result.getPath().contains("images"));
    }

    @Test
    void shouldCreateZipFile() {
        // Given
        File imagesDir = new File("/tmp/videos/" + videoId + "/images");

        // When
        File result = videoProcessor.createZip(videoId, imagesDir);

        // Then
        assertNotNull(result);
        assertTrue(result.getPath().contains(videoId.toString()));
        assertTrue(result.getPath().contains("images.zip"));
    }

    @Test
    void shouldCreateZipFileEvenWhenDirectoryDoesNotExist() {
        // Given
        File nonExistentDir = new File("/non/existent/directory");

        // When
        File result = videoProcessor.createZip(videoId, nonExistentDir);

        // Then
        assertNotNull(result);
        assertTrue(result.getPath().contains(videoId.toString()));
        assertTrue(result.getPath().contains("images.zip"));
    }

    @Test
    void shouldHandleDifferentVideoIds() {
        // Given
        UUID anotherVideoId = UUID.randomUUID();

        // When
        File result1 = videoProcessor.extractImages(videoId, storagePath);
        File result2 = videoProcessor.extractImages(anotherVideoId, storagePath);

        // Then
        assertNotEquals(result1.getPath(), result2.getPath());
        assertTrue(result1.getPath().contains(videoId.toString()));
        assertTrue(result2.getPath().contains(anotherVideoId.toString()));
    }

    @Test
    void shouldReturnConsistentPathsForSameVideoId() {
        // When
        File imagesDir = videoProcessor.extractImages(videoId, storagePath);
        File zipFile = videoProcessor.createZip(videoId, imagesDir);

        // Then
        String expectedBasePath = videoId.toString();
        assertTrue(imagesDir.getPath().contains(expectedBasePath));
        assertTrue(imagesDir.getPath().contains("images"));
        assertTrue(zipFile.getPath().contains(expectedBasePath));
        assertTrue(zipFile.getPath().contains("images.zip"));
    }
}
