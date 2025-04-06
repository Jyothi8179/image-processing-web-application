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

        String originalFileName = file.getOriginalFilename().replace(' ','-');
        String originalFileExtn =  originalFileName.split("\\.")[1];
        if(resizedFileName==null || resizedFileName == "" || !resizedFileName.contains(".")){
            resizedFileName = originalFileName.split("\\.")[0] + "-resized." + originalFileExtn;
        }

        if(originalFileExtn.equalsIgnoreCase("jpeg")|| originalFileExtn.equalsIgnoreCase("jpg")){
            resizedFileName = resizedFileName.split("\\.")[0] + "." + originalFileExtn;
        }

        String resizeFileExtn = resizedFileName.split("\\.")[1];
        if(!originalFileExtn.equalsIgnoreCase(resizeFileExtn)){
            throw new Exception("uploaded/resize file extension didn't matched");
        }

        // Ensure the upload directory exists
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Save the file to the local storage
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        // Adding time stamp to filename which to uniquely identity the images (as if multiple imagw may have same name)
        String filePath = UPLOAD_DIR + File.separator + timestamp+"_"+originalFileName;
        file.transferTo(new File(filePath));

        // Save image metadata to the database
        Image image = new Image();
        image.setName(originalFileName);
        image.setImageSize(file.getSize());
        image.setResizedImageName(resizedFileName);
        image.setOriginalFilePath(filePath);
        image.setHeight(height);
        image.setWidth(width);
        image.setResizedStatus(false); // Initially set to false until resized

        return imageRepository.save(image);
    }

    public Image saveImageMetaData(Image image){
        return imageRepository.save(image);
    }

    public boolean isImageResized(Long id){
        return imageRepository.getResizedStatusById(id);
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
