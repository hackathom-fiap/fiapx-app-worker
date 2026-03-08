package com.fiapx.processor.infrastructure.adapter.messaging;

import com.fiapx.processor.application.usecase.ProcessVideoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoProcessConsumerTest {

    @Mock
    private ProcessVideoUseCase processVideoUseCase;

    @InjectMocks
    private VideoProcessConsumer videoProcessConsumer;

    private Map<String, Object> validMessage;
    private UUID videoId;

    @BeforeEach
    void setUp() {
        videoId = UUID.randomUUID();
        validMessage = Map.of(
            "id", videoId.toString(),
            "storagePath", "/tmp/video.mp4",
            "userEmail", "test@example.com",
            "contentType", "video/mp4"
        );
    }

    @Test
    void shouldConsumeValidMessage() {
        // When
        videoProcessConsumer.consume(validMessage);

        // Then
        verify(processVideoUseCase).execute(
            eq(videoId),
            eq("/tmp/video.mp4"),
            eq("test@example.com"),
            eq("video/mp4")
        );
    }

    @Test
    void shouldHandleMessageWithNullUserEmail() {
        // Given
        Map<String, Object> message = new HashMap<>();
        message.put("id", videoId.toString());
        message.put("storagePath", "/tmp/video.mp4");
        message.put("userEmail", null);
        message.put("contentType", "video/mp4");

        // When & Then - Não deve lançar exceção
        assertDoesNotThrow(() -> {
            videoProcessConsumer.consume(message);
        });

        verify(processVideoUseCase).execute(
                eq(videoId),
                eq("/tmp/video.mp4"),
                eq(null),
                eq("video/mp4")
        );
    }

    @Test
    void shouldHandleMessageWithNullContentType() {
        // Given
        Map<String, Object> message = new HashMap<>();
        message.put("id", videoId.toString());
        message.put("storagePath", "/tmp/video.mp4");
        message.put("userEmail", "test@example.com");
        message.put("contentType", null);

        // When & Then - Não deve lançar exceção
        assertDoesNotThrow(() -> {
            videoProcessConsumer.consume(message);
        });

        verify(processVideoUseCase).execute(
                eq(videoId),
                eq("/tmp/video.mp4"),
                eq("test@example.com"),
                eq(null)
        );
    }

    @Test
    void shouldHandleMessageWithNullStoragePath() {
        // Given
        Map<String, Object> message = new HashMap<>();
        message.put("id", videoId.toString());
        message.put("storagePath", null);
        message.put("userEmail", "test@example.com");
        message.put("contentType", "video/mp4");

        // When & Then - Não deve lançar exceção
        assertDoesNotThrow(() -> {
            videoProcessConsumer.consume(message);
        });

        verify(processVideoUseCase).execute(
                eq(videoId),
                eq(null),
                eq("test@example.com"),
                eq("video/mp4")
        );
    }

    @Test
    void shouldHandleInvalidUuidGracefully() {
        // Given
        Map<String, Object> message = Map.of(
                "id", "invalid-uuid",
                "storagePath", "/tmp/video.mp4",
                "userEmail", "test@example.com",
                "contentType", "video/mp4"
        );

        // When & Then - Should handle gracefully without throwing
        assertDoesNotThrow(() -> {
            videoProcessConsumer.consume(message);
        });

        verify(processVideoUseCase, never()).execute(any(), any(), any(), any());
    }

    @Test
    void shouldHandleMissingIdGracefully() {
        // Given
        Map<String, Object> message = Map.of(
            "storagePath", "/tmp/video.mp4",
            "userEmail", "test@example.com",
            "contentType", "video/mp4"
        );

        // When & Then - Should handle gracefully without throwing
        assertDoesNotThrow(() -> {
            videoProcessConsumer.consume(message);
        });

        verify(processVideoUseCase, never()).execute(any(), any(), any(), any());
    }

    @Test
    void shouldHandleEmptyMessageGracefully() {
        // Given
        Map<String, Object> message = Map.of();

        // When & Then - Should handle gracefully without throwing
        assertDoesNotThrow(() -> {
            videoProcessConsumer.consume(message);
        });

        verify(processVideoUseCase, never()).execute(any(), any(), any(), any());
    }

    @Test
    void shouldHandleMessageWithDifferentDataTypes() {
        // Given
        Map<String, Object> message = Map.of(
            "id", videoId.toString(),
            "storagePath", "/tmp/video.mp4",
            "userEmail", "test@example.com",
            "contentType", "video/quicktime"
        );

        // When
        videoProcessConsumer.consume(message);

        // Then
        verify(processVideoUseCase).execute(
            eq(videoId),
            eq("/tmp/video.mp4"),
            eq("test@example.com"),
            eq("video/quicktime")
        );
    }

    @Test
    void shouldHandleNullIdGracefully() {
        // Given
        Map<String, Object> message = Map.of(
                "storagePath", "/tmp/video.mp4",
                "userEmail", "test@example.com",
                "contentType", "video/mp4"
        );

        // When & Then - Não deve lançar exceção
        assertDoesNotThrow(() -> {
            videoProcessConsumer.consume(message);
        });

        verify(processVideoUseCase, never()).execute(any(), any(), any(), any());
    }
}
