package com.image.conversion.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.image.resize.service.ImageResizeService.RESIZED_FILE_DIR;

@Slf4j
@Service
public class ImageConversionService {

    Logger logger = LoggerFactory.getLogger(ImageConversionService.class);
    public String convertImage(String originalFilePath, Integer width, Integer height, String convertedFileName) throws IOException {
            // Ensure output directory exists
            File resizedFolder = new File("/resize/resized");
            if (!resizedFolder.exists()) {
                resizedFolder.mkdirs();
            }
            logger.info("Original file path : "+ originalFilePath);
            // Original file path
            File originalFile = new File(originalFilePath);
            String originalFileName = originalFile.getName();

            // Check if the file exists
            if (!originalFile.exists()) {
                logger.info("File not found: " + originalFileName);
                throw new IOException("File not found: " + originalFileName);
            }

            // Define resized image file name
            String resizedFileName = (convertedFileName != null) ? convertedFileName : "resized_" + originalFileName;
            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            String convertFilePath = RESIZED_FILE_DIR + File.separator + timestamp+ "-" + resizedFileName;
            System.out.println("Resized file path : "+ convertFilePath);

            // Run ImageMagick command to resize the image
            String command = String.format("magick convert %s %s", originalFilePath, convertFilePath);

            Process process = Runtime.getRuntime().exec(command);

            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    System.err.println("ImageMagick Error: " + line);
                }
            }
            try {
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new IOException("Error resizing image, ImageMagick returned non-zero exit code: " + exitCode);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Image resizing interrupted", e);
            }

            return convertFilePath;
        }
}
