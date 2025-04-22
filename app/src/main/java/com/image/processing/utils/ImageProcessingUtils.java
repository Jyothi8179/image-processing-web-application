package com.image.processing.utils;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ImageProcessingUtils {
    private static final int MAX_WIDTH = 7680;
    private static final int MAX_HEIGHT = 4320;

    public static boolean checkImageResolution(InputStream inputStream) throws IOException {
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

    public static Map getImageDimension(InputStream inputStream) throws IOException {

        Map dimensionMap = new HashMap();
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
            dimensionMap.put("width", width);
            dimensionMap.put("height", height);
        }
        return dimensionMap;
    }

    public static String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    public static String getBaseFileName(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }

}
