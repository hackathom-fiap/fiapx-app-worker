package com.fiapx.processor.infrastructure.adapter.email;

import com.fiapx.processor.infrastructure.adapter.email.EmailNotificationAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailNotificationAdapterTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUpStreams() {
        // Redireciona a saída padrão (System.out) para nosso próprio stream
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        // Restaura a saída padrão original
        System.setOut(originalOut);
    }

    @Test
    void sendErrorNotificationShouldPrintToConsole() {
        // Given
        EmailNotificationAdapter notificationAdapter = new EmailNotificationAdapter();
        String testEmail = "test@example.com";
        UUID testVideoId = UUID.randomUUID();
        String errorMessage = "Test error message";

        // When
        notificationAdapter.sendErrorNotification(testEmail, testVideoId, errorMessage);

        // Then
        String consoleOutput = outContent.toString();
        assertTrue(consoleOutput.contains("SIMULANDO ENVIO DE E-MAIL"));
        assertTrue(consoleOutput.contains("Para: " + testEmail));
        assertTrue(consoleOutput.contains("Assunto: Erro no processamento do seu vídeo"));
        assertTrue(consoleOutput.contains("Corpo: Olá, o vídeo " + testVideoId + " falhou no processamento. Erro: " + errorMessage));
    }
}
