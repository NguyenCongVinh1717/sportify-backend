package SaleManagement.VinhNguyen.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final String SENDER_EMAIL = "nguyencongvinhbv2004@gmail.com";

    @Async
    public void sendOtpEmailAsync(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // UTF-8 mã hóa chuẩn giúp không bị lỗi font tiếng Việt
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Đặt tên hiển thị rõ ràng: "Sportify Support <nguyencongvinhbv2004@gmail.com>"
            helper.setFrom(SENDER_EMAIL, "Sportify Support");
            helper.setTo(toEmail);
            helper.setSubject("[Sportify] Mã xác thực đăng ký tài khoản");

            // Nội dung HTML giúp tăng điểm uy tín với Gmail
            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px; max-width: 500px;'>"
                    + "<h2 style='color: #333;'>Chào bạn,</h2>"
                    + "<p style='color: #555;'>Cảm ơn bạn đã đăng ký tài khoản tại <b>Sportify</b>. Mã OTP xác thực của bạn là:</p>"
                    + "<div style='background-color: #f4f6f8; padding: 15px; text-align: center; border-radius: 5px; margin: 20px 0;'>"
                    + "  <span style='font-size: 28px; font-weight: bold; color: #007bff; letter-spacing: 5px;'>" + otp + "</span>"
                    + "</div>"
                    + "<p style='color: #888; font-size: 13px;'>Mã có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("====== GỬI MAIL THÀNH CÔNG ĐẾN: " + toEmail + " ======");
        } catch (Exception e) {
            System.err.println("====== LỖI GỬI MAIL SENDGRID: " + e.getMessage() + " ======");
            e.printStackTrace();
        }
    }
}