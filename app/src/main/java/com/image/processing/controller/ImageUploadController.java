package com.image.processing.controller;

import com.image.processing.entity.Image;
import com.image.processing.service.ImageService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/v1/image")
@Validated
public class ImageUploadController {

    @Autowired ImageService imageService;
    private static final long MAX_FILE_SIZE = 25 * 1000000; // 25MB
    // private static final String UPLOAD_DIR = "/resize/uploads"; // No trailing slash
    public static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads";
    public static final String RESIZED_FILE_DIR = System.getProperty("user.dir") + File.separator + "resized";;

    // This will run at application restart
    @PostConstruct
    public void createTempDirectory() throws Exception {
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            // Create directory if it doesn't exist
            boolean created = directory.mkdirs();
            if (!created) {
                throw new Exception("Failed to create upload directory.");
            }
        }
    }


    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("image") MultipartFile file,
            @RequestParam("width") @Min(1) int width,
            @RequestParam("height") @Min(1) int height,
            @RequestParam("resizedFileName") @NotBlank String resizedFileName) {

        try {
            // Validate file size
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File size exceeds 25MB limit.");
            }

            if (!file.getContentType().startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only image files are allowed.");
            }

            // Saving image for temporary use in local storage
            // Further we can store image in separate storage
            Image savedImage = imageService.saveImage(file,resizedFileName);
            return ResponseEntity.ok(savedImage);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving image: " + e.getMessage());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadImage(@RequestParam("resizedFileName") String resizedFileName) {
        try {
            // Construct the file path
            Path filePath = Paths.get(RESIZED_FILE_DIR).resolve(resizedFileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            // Check if the file exists and is readable
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Determine file type (e.g., image/png, image/jpeg)
            String contentType = "application/octet-stream"; // Default
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resizedFileName + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}