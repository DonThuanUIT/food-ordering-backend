package com.foodorderingapp.backend.controller;

import com.foodorderingapp.backend.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadImageController {
    private final StorageService storageService;

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("Received request to upload file: {}", file.getOriginalFilename());

        String imageUrl = storageService.uploadFile(file);

        Map<String, String> response = new HashMap<>();
        response.put("url", imageUrl);

        return ResponseEntity.ok(response);
    }
}
