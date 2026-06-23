package com.foodorderingapp.backend.modules.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine; // Nhúng Thymeleaf vào

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Mã xác thực OTP - Hệ thống Đặt đồ ăn KTX");

            Context context = new Context();
            context.setVariable("otpCode", otpCode);

            String htmlContent = templateEngine.process("otp-email", context);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Successfully sent OTP email to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Error sending OTP email {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendCloseShopOtpEmail(String toEmail, String shopName, String otpCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Mã OTP xác thực đóng cửa hàng vĩnh viễn - " + shopName);

            Context context = new Context();
            context.setVariable("shopName", shopName);
            context.setVariable("otpCode", otpCode);

            String htmlContent = templateEngine.process("close-shop-otp-email", context);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Successfully sent close shop OTP email to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Error sending close shop OTP email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendShopStatusHtmlEmail(String toEmail, String shopName, boolean isApproved) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Kết quả xét duyệt Quán ăn: " + shopName);

            String statusColor = isApproved ? "#28a745" : "#dc3545";
            String statusText = isApproved ? "ĐÃ ĐƯỢC PHÊ DUYỆT" : "ĐÃ BỊ TỪ CHỐI";
            String detailMessage = isApproved
                    ? "Chúc mừng bạn! Quán ăn của bạn đã đáp ứng đủ các tiêu chuẩn và chính thức được hoạt động trên hệ thống. Hãy đăng nhập và chuyển trạng thái quán sang <b>Mở cửa</b> để đón những khách hàng đầu tiên nhé."
                    : "Rất tiếc, thông tin quán ăn của bạn chưa đáp ứng đủ tiêu chuẩn của chúng tôi ở thời điểm hiện tại. Vui lòng kiểm tra lại thông tin hồ sơ hoặc liên hệ trực tiếp với Ban quản lý để được hỗ trợ.";

            Context context = new Context();
            context.setVariable("shopName", shopName);
            context.setVariable("statusColor", statusColor);
            context.setVariable("statusText", statusText);
            context.setVariable("detailMessage", detailMessage);

            String htmlContent = templateEngine.process("shop-status-email", context);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Đã gửi email HTML thông báo trạng thái quán tới: {}", toEmail);

        } catch (Exception e) {
            log.error("Lỗi khi gửi HTML email tới {}: {}", toEmail, e.getMessage());
        }
    }
}