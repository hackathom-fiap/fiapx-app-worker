package com.fiapx.processor.infrastructure.adapter.email;

import com.fiapx.processor.infrastructure.adapter.external.EmailNotificationAdapter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailNotificationAdapterTest {

    @Mock
    private SesClient sesClient;

    @InjectMocks
    private EmailNotificationAdapter notificationAdapter;

    @Test
    void sendErrorNotificationShouldCallSes() {
        // Given
        String testEmail = "test@example.com";
        UUID testVideoId = UUID.randomUUID();
        String errorMessage = "Test error message";
        ReflectionTestUtils.setField(notificationAdapter, "senderEmail", "sender@test.com");

        // When
        notificationAdapter.sendErrorNotification(testEmail, testVideoId, errorMessage);

        // Then
        verify(sesClient).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void shouldNotCallSesWhenEmailIsEmpty() {
        // When
        notificationAdapter.sendErrorNotification("", UUID.randomUUID(), "error");

        // Then
        verify(sesClient, never()).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void shouldHandleSesExceptionGracefully() {
        // Given
        String testEmail = "test@example.com";
        ReflectionTestUtils.setField(notificationAdapter, "senderEmail", "sender@test.com");
        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenThrow(software.amazon.awssdk.services.ses.model.SesException.builder().message("SES error").build());

        // When & Then - Não deve lançar exceção para fora
        assertDoesNotThrow(() -> {
            notificationAdapter.sendErrorNotification(testEmail, UUID.randomUUID(), "error");
        });
    }
}
