package SaleManagement.VinhNguyen.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${spring.mail.password}") // Chuỗi API Key dạng SG.xxx
    private String sendGridApiKey;

    private final String SENDER_EMAIL = "nguyencongvinhbv2004@gmail.com";

    @Async
    public void sendOtpEmailAsync(String toEmail, String otp) {
        Email from = new Email(SENDER_EMAIL, "Sportify Support");
        String subject = "[Sportify Style] Mã xác thực đăng ký tài khoản";
        Email to = new Email(toEmail);

        String htmlBody = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px; max-width: 500px;'>"
                + "<h2 style='color: #333;'>Chào bạn,</h2>"
                + "<p style='color: #555;'>Mã OTP để xác thực tài khoản Sportify của bạn là:</p>"
                + "<div style='background-color: #f4f6f8; padding: 15px; text-align: center; border-radius: 5px; margin: 20px 0;'>"
                + "  <span style='font-size: 28px; font-weight: bold; color: #007bff; letter-spacing: 5px;'>" + otp + "</span>"
                + "</div>"
                + "<p style='color: #888; font-size: 13px;'>Mã có hiệu lực trong vòng 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.</p>"
                + "</div>";

        Content content = new Content("text/html", htmlBody);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            // Gửi qua HTTPS API (Port 443)
            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("====== GỬI MAIL THÀNH CÔNG QUA SENDGRID API ĐẾN: " + toEmail + " ======");
            } else {
                System.err.println("====== LỖI GỬI MAIL SENDGRID API (Code " + response.getStatusCode() + "): " + response.getBody() + " ======");
            }
        } catch (IOException ex) {
            System.err.println("====== LỖI KẾT NỐI SENDGRID API: " + ex.getMessage() + " ======");
            ex.printStackTrace();
        }
    }
}