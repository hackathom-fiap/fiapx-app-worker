package com.fiapx.processor.infrastructure.adapter.messaging;

import com.fiapx.processor.application.usecase.ProcessVideoUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class VideoProcessConsumer {

    private final ProcessVideoUseCase processVideoUseCase;

    @RabbitListener(queues = "video-process-queue")
    public void consume(Map<String, Object> message) {
        log.info("Mensagem recebida da fila: {}", message);

        String idStr = (String) message.get("id");
        if (idStr == null) {
            log.error("ID não encontrado na mensagem: {}", message);
            return;
        }

        UUID videoId;
        try {
            videoId = UUID.fromString(idStr);
        } catch (IllegalArgumentException e) {
            log.error("UUID inválido na mensagem: {}", idStr, e);
            return;
        }
        String storagePath = (String) message.get("storagePath");
        String userEmail = (String) message.get("userEmail");
        String contentType = (String) message.get("contentType");

        processVideoUseCase.execute(videoId, storagePath, userEmail, contentType);
    }
}
