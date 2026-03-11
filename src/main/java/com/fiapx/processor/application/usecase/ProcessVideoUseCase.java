package com.fiapx.processor.application.usecase;

import com.fiapx.processor.application.service.S3DownloaderService;
import com.fiapx.processor.application.service.S3UploaderService;
import com.fiapx.processor.domain.service.VideoApiPort;
import com.fiapx.processor.domain.service.VideoProcessingPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessVideoUseCase {

    private final VideoProcessingPort videoProcessing;
    private final VideoApiPort videoApi;
    private final com.fiapx.processor.domain.service.NotificationPort notification;
    private final S3UploaderService s3Uploader;
    private final S3DownloaderService s3Downloader;

    @Value("${s3.bucket.name}")
    private String s3BucketName;

    public void execute(UUID videoId, String storagePath, String userEmail, String contentType) {
        File downloadedVideo = null;
        try {
            log.info("Iniciando processamento do vídeo: {} | Tipo: {} | Usuário: {}", videoId, contentType, userEmail);
            videoApi.updateStatus(videoId, "PROCESSING", null);

            // 0. Fazer download do vídeo do S3
            String s3Key = storagePath.substring(storagePath.lastIndexOf("uploads/"));
            downloadedVideo = s3Downloader.downloadFile(s3BucketName, s3Key);

            if (contentType != null && !contentType.startsWith("video/")) {
                throw new RuntimeException("Detetada fraude de formato! O arquivo enviado é um " + contentType + " e não um vídeo real.");
            }

            // 1. Extrair imagens (FFmpeg) usando o arquivo baixado
            File imagesDir = videoProcessing.extractImages(videoId, downloadedVideo.getAbsolutePath());

            // 2. Criar ZIP
            File zipFile = videoProcessing.createZip(videoId, imagesDir);

            // 3. Fazer upload do ZIP para o S3
            String zipS3Key = "processed/" + videoId.toString() + ".zip";
            s3Uploader.uploadFile(s3BucketName, zipS3Key, zipFile.getAbsolutePath());
            String s3Url = String.format("https://%s.s3.amazonaws.com/%s", s3BucketName, zipS3Key);

            // 4. Notificar conclusão com a URL do S3
            videoApi.updateStatus(videoId, "COMPLETED", s3Url);
            log.info("Processamento concluído para o vídeo: {}. Arquivo disponível em: {}", videoId, s3Url);

        } catch (Exception e) {
            log.error("Erro ao processar vídeo: {}", videoId, e);
            videoApi.updateStatus(videoId, "ERROR", null);
            if (userEmail != null && !userEmail.isBlank()) {
                notification.sendErrorNotification(userEmail, videoId, e.getMessage());
            }
        } finally {
            // Limpa o arquivo de vídeo baixado
            if (downloadedVideo != null) {
                boolean deleted = downloadedVideo.delete();
                if (deleted) {
                    log.debug("Arquivo temporário removido com sucesso: {}", downloadedVideo.getAbsolutePath());
                } else {
                    log.warn("Falha ao remover arquivo temporário: {}", downloadedVideo.getAbsolutePath());
                }
            }
        }
    }
}
