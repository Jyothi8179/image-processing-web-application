package com.image.processing.service;

import com.image.processing.entity.Image;
import com.image.processing.repository.ImageRepository;
import com.image.resize.service.ImageResizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import static com.image.processing.controller.ImageUploadController.UPLOAD_DIR;


@Service
public class ImageService {

    @Autowired ImageRepository imageRepository;
    @Autowired ImageResizeService imageResizeService;

    public Image saveImage(MultipartFile file, String resizedFileName, int width, int height) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        String originalFileName = file.getOriginalFilename().replace(' ', '-');
        String originalFileExtn = getFileExtension(originalFileName);
        String baseFileName = getBaseFileName(originalFileName);

        if (originalFileExtn.isEmpty()) {
            throw new Exception("Uploaded file must have an extension");
        }

        // Handle missing or invalid resized file name
        if (resizedFileName == null || resizedFileName.trim().isEmpty() || !resizedFileName.contains(".")) {
            resizedFileName = baseFileName + "-resized." + originalFileExtn;
        }

        // Enforce extension consistency
        String resizedFileExtn = getFileExtension(resizedFileName);
        if (!originalFileExtn.equalsIgnoreCase(resizedFileExtn)) {
            throw new Exception("Uploaded and resized file extensions do not match");
        }

        // Normalize file extension for JPEG
        if (originalFileExtn.equalsIgnoreCase("jpeg") || originalFileExtn.equalsIgnoreCase("jpg")) {
            resizedFileName = getBaseFileName(resizedFileName) + "." + originalFileExtn;
        }

        // Ensure the upload directory exists
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Generate file path with timestamp
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filePath = UPLOAD_DIR + File.separator + timestamp + "_" + originalFileName;

        // Save to disk
        file.transferTo(new File(filePath));

        // Save metadata to database
        Image image = new Image();
        image.setName(originalFileName);
        image.setImageSize(file.getSize());
        image.setResizedImageName(resizedFileName);
        image.setOriginalFilePath(filePath);
        image.setHeight(height);
        image.setWidth(width);
        image.setResizedStatus(false);

        return imageRepository.save(image);
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    private String getBaseFileName(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }

    public Image findById(Long id) {
        return imageRepository.findById(id).orElse(null);
    }

    @Async
    public void resizeImage(Image image){

        try {
            System.out.println("Image Resize started for Image Id : "+ image.getId()+ ", name : "+image.getName());
            System.out.println(image);
            System.out.println(image.getOriginalFilePath()+", "+ image.getWidth()+","+ image.getHeight()+","+ image.getResizedImageName());
            String resizedFilePath  = imageResizeService.resizeImage(image.getOriginalFilePath(), image.getWidth(), image.getHeight(), image.getResizedImageName());
            image.setResizedFilePath(resizedFilePath);
            image.setResizedStatus(true);
            imageRepository.save(image);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("ExceptionOccured while resizing image : "+ e.getCause()+", "+e.getMessage());
        }
    }
}
