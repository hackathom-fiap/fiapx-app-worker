package com.fiapx.processor.infrastructure.adapter.processor;

import com.fiapx.processor.domain.service.VideoProcessingPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class FFmpegVideoProcessor implements VideoProcessingPort {

    @Override
    public File extractImages(UUID videoId, String storagePath) {
        log.info("Simulando extração de imagens para o vídeo: {}", videoId);
        File dir = new File("/tmp/videos/" + videoId + "/images");
        dir.mkdirs();
        return dir;
    }

    @Override
    public File createZip(UUID videoId, File imagesDir) {
        log.info("Simulando criação de ZIP para o vídeo: {}", videoId);
        File zipFile = new File("/tmp/videos/" + videoId + "/images.zip");
        try {
            zipFile.createNewFile();
        } catch (IOException e) {
            log.error("Erro ao criar zip", e);
        }
        return zipFile;
    }
}
