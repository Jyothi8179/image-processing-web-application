package com.image.resize.service;

import io.micrometer.common.util.internal.logging.Slf4JLoggerFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


@Slf4j
@Service
public class ImageResizeService {
    Logger logger = LoggerFactory.getLogger(ImageResizeService.class);
    public static final String RESIZED_FILE_DIR = System.getProperty("user.dir") + File.separator + "resized";

    // This will run at application restart
    @PostConstruct
    public void createTempDirectory() throws Exception {
        File directory = new File(RESIZED_FILE_DIR);
        if (!directory.exists()) {
            // Create directory if it doesn't exist
            boolean created = directory.mkdirs();
            if (!created) {
                throw new Exception("Failed to create upload directory.");
            }
        }
    }


    public String resizeImage(String originalImagePath, int width, int height, String resizeImageName) throws IOException {
        // Ensure output directory exists

        logger.info("Image resizing started : "+ originalImagePath);
        File resizedFolder = new File("/resize/resized");
        if (!resizedFolder.exists()) {
            resizedFolder.mkdirs();
        }
        logger.info("Original image path : "+ originalImagePath);
        // Original file path
        File originalFile = new File(originalImagePath);
        String originalFileName = originalFile.getName();

        // Check if the file exists
        if (!originalFile.exists()) {
            logger.info("File not found: " + originalFileName);
            throw new IOException("File not found: " + originalFileName);
        }

        // Define resized image file name
        String resizedFileName = (resizeImageName != null) ? resizeImageName : "resized_" + originalFileName;
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        String resizedFilePath = RESIZED_FILE_DIR + File.separator + timestamp+ "-" + resizedFileName;
        logger.info("Resized file path : "+ resizedFilePath);

        // Run ImageMagick command to resize the image
        String command = String.format("magick %s -resize %dx%d %s",
               originalImagePath,
                width,
                height,
                resizedFilePath);

        Process process = Runtime.getRuntime().exec(command);

        try (BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()))) {
            String line;
            while ((line = errorReader.readLine()) != null) {
                logger.error("ImageMagick Error: " + line);
            }
        }
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Error resizing image, ImageMagick returned non-zero exit code: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
            throw new IOException("Image resizing interrupted", e);
        }

        return resizedFilePath;
    }
}
