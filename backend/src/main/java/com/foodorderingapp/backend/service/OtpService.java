package com.foodorderingapp.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;

    private static final long OTP_VALIDITY_MINUTES = 5;


    public String generateAndSaveOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));

        String redisKey = "otp:register:" + email;

        redisTemplate.opsForValue().set(redisKey, otp, OTP_VALIDITY_MINUTES, TimeUnit.MINUTES);

        log.info("Generated OTP {} for email {} (Expires in 5 minutes)", otp, email);
        return otp;
    }


    public boolean validateOtp(String email, String inputOtp) {
        String redisKey = "otp:register:" + email;

        String savedOtp = (String) redisTemplate.opsForValue().get(redisKey);

        if (savedOtp != null && savedOtp.equals(inputOtp)) {
            redisTemplate.delete(redisKey);
            return true;
        }
        return false;
    }
}