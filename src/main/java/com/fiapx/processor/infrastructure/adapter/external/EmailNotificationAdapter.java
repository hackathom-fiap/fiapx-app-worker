package com.fiapx.processor.infrastructure.adapter.external;

import com.fiapx.processor.domain.service.NotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class EmailNotificationAdapter implements NotificationPort {

    @Override
    public void sendErrorNotification(String email, UUID videoId, String errorMessage) {
        log.info("----------------------------------------------------------------");
        log.info("SIMULANDO ENVIO DE E-MAIL");
        log.info("Para: {}", email);
        log.info("Assunto: Erro no processamento do seu vídeo");
        log.info("Corpo: Olá, o vídeo {} falhou no processamento. Erro: {}", videoId, errorMessage);
        log.info("----------------------------------------------------------------");
    }
}
