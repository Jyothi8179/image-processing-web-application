package com.image.resize.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


@Service
public class ImageResizeService {
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

        System.out.println("<==>");
        File resizedFolder = new File("/resize/resized");
        if (!resizedFolder.exists()) {
            resizedFolder.mkdirs();
        }
        System.out.println("Original image path : "+ originalImagePath);
        // Original file path
        File originalFile = new File(originalImagePath);
        String originalFileName = originalFile.getName();

        // Check if the file exists
        if (!originalFile.exists()) {
            System.out.println("File not found: " + originalFileName);
            throw new IOException("File not found: " + originalFileName);
        }

        // Define resized image file name
        String resizedFileName = (resizeImageName != null) ? resizeImageName : "resized_" + originalFileName;
        String resizedFilePath = RESIZED_FILE_DIR + File.separator +resizedFileName;
        System.out.println("Resized file path : "+ resizedFilePath);

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

        return resizedFilePath;
    }
}
