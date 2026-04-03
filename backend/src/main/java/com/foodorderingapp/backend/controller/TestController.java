package com.foodorderingapp.backend.controller;

import com.foodorderingapp.backend.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/business-error")
    public String testBusinessError() {
        throw new AppException("This phone number is registered!", HttpStatus.BAD_REQUEST);
    }
    @GetMapping("/system-error")
    public String testSystemError() {
        int a = 10 / 0;
        return "Result: " + a;
    }
    @GetMapping
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Backend Food Order App ");
    }
}