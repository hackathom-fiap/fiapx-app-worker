package com.fiapx.processor.domain.service;

import java.util.UUID;

public interface VideoApiPort {
    void updateStatus(UUID id, String status, String storagePath);
}
