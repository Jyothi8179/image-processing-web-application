package com.image.processing.service;

import com.image.conversion.service.ImageConversionService;
import com.image.processing.entity.Image;
import com.image.processing.repository.ImageRepository;
import com.image.processing.utils.ImageProcessingUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import static com.image.processing.controller.ImageUploadController.UPLOAD_DIR;

@Slf4j
@Service
public class ImageConvertorService {

    Logger logger = LoggerFactory.getLogger(ImageConvertorService.class);
    @Autowired ImageRepository imageRepository;
    @Autowired ImageConversionService imageConversionService;

    public Image saveImage(MultipartFile file, String newFileName, int width, int height) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        String originalFileName = file.getOriginalFilename().replace(' ', '-');
        String originalFileExtn = ImageProcessingUtils.getFileExtension(originalFileName);
        String baseFileName = ImageProcessingUtils.getBaseFileName(originalFileName);

        if (originalFileExtn.isEmpty()) {
            throw new Exception("Uploaded file must have an extension");
        }

        // Handle missing or invalid resized file name
        if (newFileName == null || newFileName.trim().isEmpty() || !newFileName.contains(".")) {
            newFileName = baseFileName + "-converted." + originalFileExtn;
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
        image.setConvertedFileName(newFileName);
        image.setOriginalFilePath(filePath);
        image.setHeight(height);
        image.setWidth(width);
        image.setConversionStatus(false);

        return imageRepository.save(image);
    }


    public void convertImage(Image image) {
        try {
            logger.info("Image Conversion started for Image Id : "+ image.getId()+ ", name : "+image.getName());
            logger.info(image.getOriginalFilePath()+", "+ image.getWidth()+","+ image.getHeight()+","+ image.getConvertedFileName());
            String convertedFilePath  = imageConversionService.convertImage(image.getOriginalFilePath(), image.getWidth(), image.getHeight(), image.getConvertedFileName());
            image.setConvertedFilePath(convertedFilePath);
            image.setConversionStatus(true);
            imageRepository.save(image);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("ExceptionOccured while resizing image : "+ e.getCause()+", "+e.getMessage());
        }
    }
}
