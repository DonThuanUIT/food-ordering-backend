package com.foodorderingapp.backend.modules.upload;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface StorageService {
    String uploadFile(MultipartFile file) throws IOException;
}
