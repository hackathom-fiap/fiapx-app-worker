package com.fiapx.processor.domain.service;

import java.util.UUID;

public interface NotificationPort {
    void sendErrorNotification(String email, UUID videoId, String errorMessage);
}
