package com.image.resize.service;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@Service
public class ImageResizeService {


    static {
        try{
            byte[] res = resizeImage("test.jpeg", 1080,720, "resized.jpeg");
            System.out.println(res);

        }catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getCause());
            System.out.println(e.getMessage());

        }
    }

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";
    private static final String RESIZED_DIR = System.getProperty("user.dir") + "/resized/";

    public static byte[] resizeImage(String originalFileName, int width, int height, String outputName) throws IOException {
        // Ensure output directory exists

        System.out.println("<==>");
        File resizedFolder = new File("/resize/resized");
        if (!resizedFolder.exists()) {
            resizedFolder.mkdirs();
        }

        // Original file path
        String originalFilePath = "/resize/uploads/" + originalFileName;
        File originalFile = new File(originalFilePath);

        // Check if the file exists
        if (!originalFile.exists()) {
            System.out.println("File not found: " + originalFilePath);
            throw new IOException("File not found: " + originalFilePath);
        }

        // Define resized image file name
        String resizedFileName = (outputName != null) ? outputName : "resized_" + originalFileName;
        String resizedFilePath = "/resize/resized/" + resizedFileName;

        // Run ImageMagick command to resize the image
        String command = String.format("convert %s -resize %dx%d %s",
                originalFilePath, width, height, resizedFilePath);

        Process process = Runtime.getRuntime().exec(command);
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Error resizing image, ImageMagick returned non-zero exit code: " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Image resizing interrupted", e);
        }

        // Read and return resized image as byte array
        byte[] resizedImage = FileUtils.readFileToByteArray(new File(resizedFilePath));

        // Clean up temporary resized file
        Files.deleteIfExists(Path.of(resizedFilePath));

        return resizedImage;
    }


}
