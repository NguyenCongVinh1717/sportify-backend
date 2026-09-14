package SaleManagement.VinhNguyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendOtpEmailAsync(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("[Sportify Style] Mã kích hoạt tài khoản thành viên");
            message.setText("Chào bạn,\n\nMã OTP để xác thực đăng ký tài khoản của bạn tại Sportify là: "
                    + otp + "\n\nMã có hiệu lực trong vòng 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.");
            mailSender.send(message);
            System.out.println("====== GỬI MAIL THÀNH CÔNG ĐẾN: " + toEmail + " ======");
        } catch (Exception e) {
            System.err.println("====== LỖI GỬI MAIL SMTP: " + e.getMessage() + " ======");
            e.printStackTrace();
        }
    }
}