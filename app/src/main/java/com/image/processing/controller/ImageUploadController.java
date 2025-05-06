package com.image.processing.controller;

import com.app.entity.UserData;
import com.app.service.SelfPingService;
import com.app.service.UserDataService;
import com.image.processing.entity.Image;
import com.image.processing.service.ImageService;

import com.image.processing.utils.ImageProcessingUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;


import jakarta.validation.constraints.Min;

import javax.imageio.ImageIO;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/v1/image")
@Validated
public class ImageUploadController {


    @Value("${auth.cleanup.token}")
    String expectedToken;

    @Value("${rate.limit}")
    String limit;

    Logger logger = LoggerFactory.getLogger(ImageUploadController.class);
    @Autowired ImageService imageService;
    @Autowired SelfPingService selfPingService;
    @Autowired UserDataService userDataService;



    private static final int MAX_WIDTH = 7680;
    private static final int MAX_HEIGHT = 4320;

    private static final long MAX_FILE_SIZE = 25 * 1000000; // 25MB
    public static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads";

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
            @RequestParam("width")@Validated @Min(1) @Max(7680) int width,
            @RequestParam("height") @Validated @Min(1) @Max(4320) int height,
            @RequestParam(value = "targetImageSize", defaultValue = "-1") int  targetImageSize,
            @RequestParam(value = "resizedFileName", required = false) String resizedFileName,
            HttpServletRequest request) {

        String ip = getClientIp(request);
        UserData userData = userDataService.getUserData(ip);
        if(userData!=null && userData.getCount() >= Integer.parseInt(limit)){
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Maximum upload limit is : ".concat(String.valueOf(limit)));
        }else{
            userDataService.save(ip);
        }

        try {
            // Validate file size
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File size exceeds 25MB limit.");
            }

            if (!file.getContentType().startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only image files are allowed.");
            }

            boolean isValid = ImageProcessingUtils.checkImageResolution(file.getInputStream());
            if (!isValid) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Image resolution exceeds the maximum allowed (7680x4320).");
            }

            // Saving image for temporary use in local storage
            // Further we can store image in separate storage
            Image savedImage = imageService.saveImage(file,resizedFileName, width, height, targetImageSize);
            if(savedImage!=null){
                imageService.resizeImage(savedImage);
            }
            logger.info("Image uploaded sucessfully for Image Id : "+ savedImage.getId()+ ", name : "+savedImage.getName());
            return ResponseEntity.ok(savedImage);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving image: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error : " + e.getMessage());
        }

    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For may contain multiple IPs, take the first
            return ip.split(",")[0];
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }

    @GetMapping("/download")
    public ResponseEntity<?> downloadImage(
            @RequestParam("id") Long id) {
        try {

            Image image = imageService.findById(id);
            if(image.getResizedStatus()!=null && image.getResizedStatus()==false){
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Image  : " + image.getName() + " is not resized yet.");
            }

            if(image.getConversionStatus()!=null && image.getConversionStatus()==false){
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Image  : " + image.getName() + " is not resized yet.");
            }


            // Construct the file path
            String resizedFileName;
            Path filePath;
            Resource resource;

            if(image.getConversionStatus()!=null && image.getConversionStatus()){
                resizedFileName = image.getConvertedFileName();
                filePath = Paths.get(image.getConvertedFilePath());
                resource = new UrlResource(filePath.toUri());
            }else{
                resizedFileName = image.getResizedImageName();
                filePath = Paths.get(image.getResizedFilePath());
                resource = new UrlResource(filePath.toUri());
            }

            // Check if the file exists and is readable
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            String contentType = "application/octet-stream";
            String encodedFileName = URLEncoder.encode(resizedFileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private boolean checkImageResolution(InputStream inputStream) throws IOException {
        // Get ImageReader from ImageIO
        ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
        var readers = ImageIO.getImageReaders(imageInputStream);

        if (readers.hasNext()) {
            var reader = readers.next();
            reader.setInput(imageInputStream);

            // Get image metadata and check the resolution
            IIOMetadata metadata = reader.getImageMetadata(0);
            var formatName = metadata.getNativeMetadataFormatName();

            // Check width and height (avoid loading the whole image into memory)
            int width = reader.getWidth(0);
            int height = reader.getHeight(0);

            return width <= MAX_WIDTH && height <= MAX_HEIGHT;
        }

        return false;  // Invalid image if no readers were found
    }

    @DeleteMapping("/clean-up")
    public ResponseEntity<String> testing(@RequestParam(value = "token") String token){ // ideally we should take it from @Header/@Auth Body

        if(!token.equals(expectedToken)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Invalid token");
        }
        selfPingService.cleanUp();
        return ResponseEntity.noContent().build();
    }

}