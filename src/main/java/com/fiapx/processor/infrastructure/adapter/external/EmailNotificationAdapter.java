package com.fiapx.processor.infrastructure.adapter.external;

import com.fiapx.processor.domain.service.NotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationAdapter implements NotificationPort {

    private final SesClient sesClient;

    @Value("${ses.sender.email:hmouraf@hotmail.com}")
    private String senderEmail;

    @Override
    public void sendErrorNotification(String email, UUID videoId, String errorMessage) {
        if (email == null || email.isBlank()) {
            log.warn("E-mail de destino não fornecido para o vídeo: {}", videoId);
            return;
        }

        log.info("Enviando e-mail real via SES para: {}", email);

        String subject = "Erro no processamento do seu vídeo";
        String body = String.format("Olá, o vídeo %s falhou no processamento.\n\nErro: %s", videoId, errorMessage);

        try {
            SendEmailRequest request = SendEmailRequest.builder()
                    .destination(Destination.builder().toAddresses(email).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).build())
                            .body(Body.builder().text(Content.builder().data(body).build()).build())
                            .build())
                    .source(senderEmail)
                    .build();

            sesClient.sendEmail(request);
            log.info("E-mail enviado com sucesso para: {}", email);

        } catch (SesException e) {
            log.error("Falha ao enviar e-mail via SES para: {}. Erro: {}", email, e.awsErrorDetails().errorMessage());
        }
    }
}
