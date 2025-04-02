package com.image.processing.service;

import com.image.processing.entity.Image;
import com.image.processing.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static com.image.processing.controller.ImageUploadController.UPLOAD_DIR;


@Service
public class ImageService {

    @Autowired ImageRepository imageRepository;

    public Image saveImage(MultipartFile file, String resizedFileName) throws IOException {
        // Ensure the upload directory exists
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Save the file to the local storage
        String filePath = UPLOAD_DIR + File.separator + file.getOriginalFilename();
        file.transferTo(new File(filePath));

        // Save image metadata to the database
        Image image = new Image();
        image.setName(file.getOriginalFilename());
        image.setImageSize(file.getSize());
        image.setResizedImageName(String.valueOf(file.getOriginalFilename().split(".")[0])+"_"+resizedFileName);
        image.setResizedStatus(false); // Initially set to false until resized

        return imageRepository.save(image);
    }

}
