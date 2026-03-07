package com.fiapx.processor.infrastructure.adapter.external;

import com.fiapx.processor.domain.service.VideoApiPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VideoApiAdapter implements VideoApiPort {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${video.api.url:http://video-api:8082/api/videos}")
    private String videoApiUrl;

    @Override
    public void updateStatus(UUID id, String status, String storagePath) {
        String url = videoApiUrl + "/" + id + "/status";
        Map<String, String> body = new HashMap<>();
        body.put("status", status);
        body.put("storagePath", storagePath);
        
        restTemplate.postForObject(url, body, Object.class);
    }
}
