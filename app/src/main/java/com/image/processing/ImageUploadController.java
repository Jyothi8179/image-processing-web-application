package com.image.processing;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/v1/image")
@Validated
public class ImageUploadController {

    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024; // 25MB
    // private static final String UPLOAD_DIR = "/resize/uploads"; // No trailing slash
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "/resize/uploads";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(
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

            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                boolean created = directory.mkdirs(); // Create directory if it doesn't exist
                if (!created) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to create upload directory.");
                }
            }

            String filePath = UPLOAD_DIR + File.separator + resizedFileName; // Use File.separator for cross-platform compatibility
            file.transferTo(new File(filePath));

            return ResponseEntity.ok("Image uploaded successfully: " + filePath);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving image: " + e.getMessage());
        }
    }
}