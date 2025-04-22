package com.app.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Service
public class CleanUpService {
    Logger logger = LoggerFactory.getLogger(CleanUpService.class);
    protected void deleteFilesInDirectory(String directoryPath) {
        try {
            Files.walk(Paths.get(directoryPath))
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception e) {
                            logger.warn("Failed to delete file: " + path + " - " + e.getMessage());
                        }
                    });
            logger.info("Deleted files in: " + directoryPath);
        } catch (Exception e) {
            logger.error("Error deleting files in: " + directoryPath + " - " + e.getMessage()+ e.getCause());
            e.printStackTrace();
        }
    }
}
