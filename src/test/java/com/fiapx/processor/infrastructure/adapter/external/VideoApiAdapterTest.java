package com.fiapx.processor.infrastructure.adapter.external;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoApiAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private VideoApiAdapter videoApiAdapter;

    private UUID videoId;
    private String status;
    private String storagePath;

    @BeforeEach
    void setUp() {
        videoId = UUID.randomUUID();
        status = "PROCESSING";
        storagePath = "/tmp/video.mp4";

        // Set the videoApiUrl field using reflection
        ReflectionTestUtils.setField(videoApiAdapter, "videoApiUrl", "http://localhost:8082/api/videos");

        // Inject the mocked RestTemplate
        ReflectionTestUtils.setField(videoApiAdapter, "restTemplate", restTemplate);
    }

    @Test
    void shouldUpdateStatusSuccessfully() {
        // Given
        String expectedUrl = "http://localhost:8082/api/videos/" + videoId + "/status";

        // When
        videoApiAdapter.updateStatus(videoId, status, storagePath);

        // Then
        verify(restTemplate).postForObject(eq(expectedUrl), any(Map.class), eq(Object.class));
    }

    @Test
    void shouldUpdateStatusWithNullStoragePath() {
        // Given
        String expectedUrl = "http://localhost:8082/api/videos/" + videoId + "/status";

        // When
        videoApiAdapter.updateStatus(videoId, status, null);

        // Then
        verify(restTemplate).postForObject(eq(expectedUrl), any(Map.class), eq(Object.class));
    }

    @Test
    void shouldUpdateStatusWithDifferentStatus() {
        // Given
        String differentStatus = "COMPLETED";
        String expectedUrl = "http://localhost:8082/api/videos/" + videoId + "/status";

        // When
        videoApiAdapter.updateStatus(videoId, differentStatus, storagePath);

        // Then
        verify(restTemplate).postForObject(eq(expectedUrl), any(Map.class), eq(Object.class));
    }

    @Test
    void shouldUseCorrectUrlFormat() {
        // Given
        UUID anotherVideoId = UUID.randomUUID();
        String expectedUrl = "http://localhost:8082/api/videos/" + anotherVideoId + "/status";

        // When
        videoApiAdapter.updateStatus(anotherVideoId, status, storagePath);

        // Then
        verify(restTemplate).postForObject(eq(expectedUrl), any(Map.class), eq(Object.class));
    }

    @Test
    void shouldSendCorrectRequestBody() {
        // Given
        String expectedUrl = "http://localhost:8082/api/videos/" + videoId + "/status";

        // When
        videoApiAdapter.updateStatus(videoId, status, storagePath);

        // Then
        verify(restTemplate).postForObject(eq(expectedUrl), argThat(body -> {
            @SuppressWarnings("unchecked")
            Map<String, String> bodyMap = (Map<String, String>) body;
            return status.equals(bodyMap.get("status")) &&
                    storagePath.equals(bodyMap.get("storagePath"));
        }), eq(Object.class));
    }

    @Test
    void shouldHandleEmptyStoragePath() {
        // Given
        String emptyStoragePath = "";
        String expectedUrl = "http://localhost:8082/api/videos/" + videoId + "/status";

        // When
        videoApiAdapter.updateStatus(videoId, status, emptyStoragePath);

        // Then
        verify(restTemplate).postForObject(eq(expectedUrl), any(Map.class), eq(Object.class));
    }
}