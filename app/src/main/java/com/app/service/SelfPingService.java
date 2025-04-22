package com.app.service;

import com.image.processing.repository.ImageRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class SelfPingService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${self.ping.url}")
    private String APP_URL;

    @Value("${cleanup.upload.dir}")
    private String uploadDir;

    @Value("${cleanup.resized.dir}")
    private String resizedDir;

    @Value("${cleanup.interval.miliseconds}")
    private final String cleanupInterval = "21600000";

    Logger logger = LoggerFactory.getLogger(SelfPingService.class);

    @PostConstruct
    public void init() {

        logger.info("[SelfPing] Initialized with URL: " + APP_URL);

//        logger.info("------------------------  [CleanUp] started  ------------------------  ");
//        cleanUp();
//        logger.info("------------------------  [CleanUp] finished  ------------------------  ");

    }
    @Autowired CleanUpService cleanUpService;
    @Autowired ImageRepository imageRepository;

    @Scheduled(fixedRate = 45_000)
    public void pingMyself() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(APP_URL, String.class);
            logger.info("[SelfPing] "+"  Status:"+  response.getStatusCode()+", Scheduled method executed at: " + java.time.LocalDateTime.now());
        } catch (Exception e) {
            logger.error("[SelfPing Error] " + e.getMessage());
        }
    }

    @Scheduled(fixedRateString = cleanupInterval) // 6 hours
    public void cleanUp(){
        logger.info("------------------------  [CleanUp] started  ------------------------  ");

        cleanUpService.deleteFilesInDirectory(uploadDir);
        cleanUpService.deleteFilesInDirectory(resizedDir);

        logger.info("Cleaning up Image table...");
        imageRepository.deleteAll();
        logger.info("Cleanup complete.");
        logger.info("------------------------  [CleanUp] finished  ------------------------  ");

    }
}
