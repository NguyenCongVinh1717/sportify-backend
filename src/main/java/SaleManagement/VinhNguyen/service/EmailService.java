package SaleManagement.VinhNguyen.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // THÊM MỚI
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Lấy email thực tế của bạn (ví dụ: nguyencongvinhbv2004@gmail.com)
    @Value("${spring.mail.sender.email:nguyencongvinhbv2004@gmail.com}")
    private String fromEmail;

    @Async
    public void sendOtpEmailAsync(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail); // <--- BẮT BUỘC THÊM DÒNG NÀY ĐỂ SENDGRID XÁC THỰC SENDER
            message.setTo(toEmail);
            message.setSubject("[Sportify Style] Mã kích hoạt tài khoản thành viên");
            message.setText("Chào bạn,\n\nMã OTP để xác thực đăng ký tài khoản của bạn tại Sportify là: "
                    + otp + "\n\nMã có hiệu lực trong vòng 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.");

            mailSender.send(message);
            System.out.println("====== GỬI MAIL THÀNH CÔNG ĐẾN: " + toEmail + " ======");
        } catch (Exception e) {
            System.err.println("====== LỖI GỬI MAIL SENDGRID: " + e.getMessage() + " ======");
            e.printStackTrace();
        }
    }
}