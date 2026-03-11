package com.fiapx.processor.infrastructure.adapter.processor;

import com.fiapx.processor.domain.service.VideoProcessingPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@Slf4j
public class FFmpegVideoProcessor implements VideoProcessingPort {

    @Override
    public File extractImages(UUID videoId, String storagePath) {
        log.info("Iniciando extração real de imagens para o vídeo: {}", videoId);
        
        File outputDir = new File("/tmp/videos/" + videoId + "/images");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Comando FFmpeg: extrair 1 frame por segundo (-vf fps=1)
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-i", storagePath,
                "-vf", "fps=1",
                outputDir.getAbsolutePath() + "/frame_%04d.jpg"
        );

        try {
            Process process = processBuilder.start();
            
            // Logar erros do FFmpeg caso ocorram
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("FFmpeg: {}", line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("FFmpeg falhou com código de saída: {}", exitCode);
            } else {
                log.info("Extração de imagens concluída com sucesso para o vídeo: {}", videoId);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Erro ao executar FFmpeg", e);
            Thread.currentThread().interrupt();
        }

        return outputDir;
    }

    @Override
    public File createZip(UUID videoId, File imagesDir) {
        log.info("Iniciando criação real de ZIP para o vídeo: {}", videoId);
        File zipFile = new File("/tmp/videos/" + videoId + "/images.zip");
        
        // Garante que o diretório pai exista
        zipFile.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            File[] files = imagesDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        addToZip(file, zos);
                    }
                }
            }
            log.info("Criação do ZIP concluída com sucesso: {}", zipFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Erro ao criar arquivo ZIP", e);
        }
        
        return zipFile;
    }

    private void addToZip(File file, ZipOutputStream zos) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zos.putNextEntry(zipEntry);
            
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }
            zos.closeEntry();
        }
    }
}
