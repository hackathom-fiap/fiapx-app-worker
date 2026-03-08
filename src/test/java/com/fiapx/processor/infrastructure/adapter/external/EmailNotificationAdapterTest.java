package com.fiapx.processor.infrastructure.adapter.external;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationAdapterTest {

    private EmailNotificationAdapter emailNotificationAdapter;

    @BeforeEach
    void setUp() {
        emailNotificationAdapter = new EmailNotificationAdapter();
    }

    @Test
    void shouldSendErrorNotificationSuccessfully() {
        // Given
        String email = "test@example.com";
        UUID videoId = UUID.randomUUID();
        String errorMessage = "Processing failed";

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            emailNotificationAdapter.sendErrorNotification(email, videoId, errorMessage);
        });
    }

    @Test
    void shouldHandleNullEmail() {
        // Given
        String email = null;
        UUID videoId = UUID.randomUUID();
        String errorMessage = "Processing failed";

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            emailNotificationAdapter.sendErrorNotification(email, videoId, errorMessage);
        });
    }

    @Test
    void shouldHandleNullVideoId() {
        // Given
        String email = "test@example.com";
        UUID videoId = null;
        String errorMessage = "Processing failed";

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            emailNotificationAdapter.sendErrorNotification(email, videoId, errorMessage);
        });
    }

    @Test
    void shouldHandleNullErrorMessage() {
        // Given
        String email = "test@example.com";
        UUID videoId = UUID.randomUUID();
        String errorMessage = null;

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            emailNotificationAdapter.sendErrorNotification(email, videoId, errorMessage);
        });
    }

    @Test
    void shouldHandleEmptyEmail() {
        // Given
        String email = "";
        UUID videoId = UUID.randomUUID();
        String errorMessage = "Processing failed";

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            emailNotificationAdapter.sendErrorNotification(email, videoId, errorMessage);
        });
    }

    @Test
    void shouldHandleLongErrorMessage() {
        // Given
        String email = "test@example.com";
        UUID videoId = UUID.randomUUID();
        String errorMessage = "This is a very long error message that could potentially cause issues but should still be handled properly by the notification system without any problems";

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            emailNotificationAdapter.sendErrorNotification(email, videoId, errorMessage);
        });
    }
}
