package com.fiapx.processor.domain.service;

import java.io.File;
import java.util.UUID;

public interface VideoProcessingPort {
    File extractImages(UUID videoId, String storagePath);
    File createZip(UUID videoId, File imagesDir);
}
