package com.image.processing.controller;

import com.app.entity.UserData;
import com.app.service.UserDataService;
import com.image.processing.entity.Image;
import com.image.processing.service.ImageConvertorService;
import com.image.processing.utils.ImageProcessingUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/image/convertor")
public class ImageConvertorController {


    @Value("${rate.limit}")
    String limit;

    private static final long MAX_FILE_SIZE = 25 * 1000000; // 25MB
    public static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads";

    private static final int MAX_WIDTH = 7680;
    private static final int MAX_HEIGHT = 4320;

    @Autowired ImageConvertorService imageConvertorService;
    @Autowired UserDataService userDataService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("image") MultipartFile file,
            @RequestParam(value = "targetFileName", required = false) String targetFileName,
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
            Map imageData = ImageProcessingUtils.getImageDimension(file.getInputStream());
            Image savedImage = imageConvertorService.saveImage(file,targetFileName, (int)imageData.get("width"), (int)imageData.get("height"));
            if(savedImage!=null){
                imageConvertorService.convertImage(savedImage);
            }
            System.out.println("Image uploaded sucessfully for Image Id : "+ savedImage.getId()+ ", name : "+savedImage.getName());
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

}
