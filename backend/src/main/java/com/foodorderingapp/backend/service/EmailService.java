package com.foodorderingapp.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Mã xác thực OTP - Hệ thống Đặt đồ ăn KTX");

            String htmlContent = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                        <h2 style="color: #2e6c80; text-align: center;">Xác thực tài khoản của bạn</h2>
                        <p>Chào bạn,</p>
                        <p>Cảm ơn bạn đã đăng ký tài khoản trên hệ thống Đặt đồ ăn KTX. Dưới đây là mã xác thực (OTP) của bạn:</p>
                        <div style="text-align: center; margin: 20px 0;">
                            <span style="font-size: 32px; font-weight: bold; color: #d9534f; letter-spacing: 5px; padding: 10px 20px; border: 2px dashed #d9534f; border-radius: 5px;">%s</span>
                        </div>
                        <p style="color: #555; font-size: 14px;"><i>Lưu ý: Mã này chỉ có hiệu lực trong vòng 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.</i></p>
                        <hr style="border-top: 1px solid #eee;" />
                        <p style="text-align: center; font-size: 12px; color: #999;">Đây là email tự động, vui lòng không trả lời lại.</p>
                    </div>
                    """.formatted(otpCode);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Successfully sent OTP email to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Error sending OTP email {}: {}", toEmail, e.getMessage());
        }
    }
}
