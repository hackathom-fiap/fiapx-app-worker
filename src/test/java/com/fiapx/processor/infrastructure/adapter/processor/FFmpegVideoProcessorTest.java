package com.fiapx.processor.infrastructure.adapter.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

class FFmpegVideoProcessorTest {

    private FFmpegVideoProcessor videoProcessor;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        videoProcessor = new FFmpegVideoProcessor();
    }

    @Test
    void createZipShouldCreateAValidZipFile() throws IOException {
        // Given
        UUID videoId = UUID.randomUUID();
        File imagesDir = tempDir.resolve("images").toFile();
        imagesDir.mkdir();

        // Create some dummy image files
        Files.createFile(imagesDir.toPath().resolve("image1.jpg"));
        Files.createFile(imagesDir.toPath().resolve("image2.jpg"));

        // When
        File zipFile = videoProcessor.createZip(videoId, imagesDir);

        // Then
        assertTrue(zipFile.exists());
        assertTrue(zipFile.length() > 0);

        // Verify the zip content
        try (ZipFile zf = new ZipFile(zipFile)) {
            assertEquals(2, zf.size());
        }
    }

    @Test
    void createZipShouldHandleEmptyDirectory() throws IOException {
        // Given
        UUID videoId = UUID.randomUUID();
        File imagesDir = tempDir.resolve("empty-images").toFile();
        imagesDir.mkdir();

        // When
        File zipFile = videoProcessor.createZip(videoId, imagesDir);

        // Then
        assertTrue(zipFile.exists());

        try (ZipFile zf = new ZipFile(zipFile)) {
            assertEquals(0, zf.size());
        }
    }

    @Test
    void createZipShouldHandleUnreadableDirectory() throws IOException {
        // Given
        UUID videoId = UUID.randomUUID();
        File imagesDir = tempDir.resolve("unreadable-images").toFile();
        imagesDir.mkdir();
        // Torna o diretório não legível para simular a falha de listFiles()
        imagesDir.setReadable(false);

        // When
        File zipFile = videoProcessor.createZip(videoId, imagesDir);

        // Then
        assertTrue(zipFile.exists());
        try (ZipFile zf = new ZipFile(zipFile)) {
            assertEquals(0, zf.size()); // O zip deve ser criado vazio
        }

        // Restaura a permissão para permitir a limpeza pelo @TempDir
        imagesDir.setReadable(true);
    }

    // Nota: Testar o método extractImages que chama um processo externo (ffmpeg)
    // é complexo e geralmente requer testes de integração ou mocks de PowerMock/ProcessBuilder,
    // o que está além do escopo de um teste unitário simples.
    // No entanto, podemos testar a parte da criação do diretório.
    @Test
    void extractImagesShouldCreateDirectory() {
        // Given
        UUID videoId = UUID.randomUUID();
        String dummyStoragePath = tempDir.resolve("dummy.mp4").toString();

        // When
        File outputDir = videoProcessor.extractImages(videoId, dummyStoragePath);

        // Then
        assertTrue(outputDir.exists());
        assertTrue(outputDir.isDirectory());
        assertTrue(outputDir.getPath().contains(videoId.toString()));
    }
}
