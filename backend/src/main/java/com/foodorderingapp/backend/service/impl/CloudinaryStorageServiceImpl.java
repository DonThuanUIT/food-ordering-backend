package com.foodorderingapp.backend.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.foodorderingapp.backend.exception.AppException;
import com.foodorderingapp.backend.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryStorageServiceImpl implements StorageService {
    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new AppException("File cannot be empty!", HttpStatus.BAD_REQUEST);
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "FoodOrderApp_Images",
                    "resource_type", "auto"
            ));

            String publicUrl = uploadResult.get("secure_url").toString();

            log.info("File uploaded successfully to Cloudinary: {}", publicUrl);
            return publicUrl;

        } catch (IOException e) {
            log.error("Failed to upload file to Cloudinary", e);
            throw new AppException("Error occurred while uploading the file. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
