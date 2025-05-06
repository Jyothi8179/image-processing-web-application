package com.image.processing.service;

import com.image.processing.entity.Image;
import com.image.processing.repository.ImageRepository;
import com.image.processing.utils.ImageProcessingUtils;
import com.image.resize.service.ImageResizeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

import static com.image.processing.controller.ImageUploadController.UPLOAD_DIR;


@Service
public class ImageService {

    Logger logger = LoggerFactory.getLogger(ImageService.class);

    @Autowired ImageRepository imageRepository;
    @Autowired ImageResizeService imageResizeService;

    public Image saveImage(MultipartFile file, String resizedFileName, int width, int height, int targetImageSize) throws Exception {
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
        if (resizedFileName == null || resizedFileName.trim().isEmpty() || !resizedFileName.contains(".")) {
            resizedFileName = baseFileName + "-resized." + originalFileExtn;
        }

        String resizedFileExtn = ImageProcessingUtils.getFileExtension(resizedFileName);

        // Normalize file extension for JPEG
        if ((originalFileExtn.equalsIgnoreCase("jpeg") || originalFileExtn.equalsIgnoreCase("jpg"))
        && (resizedFileExtn.equalsIgnoreCase("jpeg") || resizedFileExtn.equalsIgnoreCase("jpg"))) {
            resizedFileName = ImageProcessingUtils.getBaseFileName(resizedFileName) + "." + originalFileExtn;
        }

        // Enforce extension consistency
        resizedFileExtn = ImageProcessingUtils.getFileExtension(resizedFileName);
        if (!originalFileExtn.equalsIgnoreCase(resizedFileExtn)) {
            throw new Exception("Uploaded and resized file extensions do not match");
        }

        // Ensure the upload directory exists
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // CASE: targetImageSize is given
        // re-calculating height and width as if user is sending targetImageSize then they will not send width and height
        if(targetImageSize != -1){
            Map dimension = ImageProcessingUtils.getImageDimension(file.getInputStream());
            int origW = (int)dimension.get("width");
            int origH = (int)dimension.get("height");

            long origBytes = file.getSize();
            double origKb = origBytes / 1024.0;
            double ratio = targetImageSize / origKb;
            double scale = Math.sqrt(ratio);

            width = (int) Math.round(origW * scale);;
            height = (int) Math.round(origH * scale);
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
        image.setTargetImageSize(targetImageSize);
        image.setResizedStatus(false);

        return imageRepository.save(image);
    }

    public Image findById(Long id) {
        return imageRepository.findById(id).orElse(null);
    }

    @Async
    public void resizeImage(Image image){

        try {
            logger.info("Image Resize started for Image Id : "+ image.getId()+ ", name : "+image.getName());
            logger.info(image.getOriginalFilePath()+", "+ image.getWidth()+","+ image.getHeight()+","+ image.getResizedImageName());
            String resizedFilePath  = imageResizeService.resizeImage(image.getOriginalFilePath(), image.getWidth(), image.getHeight(), image.getResizedImageName(), image.getTargetImageSize());
            image.setResizedFilePath(resizedFilePath);
            image.setResizedStatus(true);
            imageRepository.save(image);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("ExceptionOccured while resizing image : "+ e.getCause()+", "+e.getMessage());
        }
    }

    public void deleteAll(){
        imageRepository.deleteAll();
    }
}
