package com.app.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SelfPingService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${self.ping.url}")
    private String APP_URL;

    @PostConstruct
    public void init() {
        System.out.println("[SelfPing] Initialized with URL: " + APP_URL);
    }

    @Scheduled(fixedRate = 45_000)
    public void pingMyself() {
        try {
            System.out.println("[SelfPing] Scheduled method executed at: " + java.time.LocalDateTime.now());
            ResponseEntity<String> response = restTemplate.getForEntity(APP_URL, String.class);
            System.out.println("[SelfPing] Status: " + response.getStatusCode());
        } catch (Exception e) {
            System.err.println("[SelfPing Error] " + e.getMessage());
        }
    }
}
