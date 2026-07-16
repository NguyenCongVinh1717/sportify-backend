package SaleManagement.VinhNguyen.service;

import SaleManagement.VinhNguyen.entity.User;
import SaleManagement.VinhNguyen.exception.AppException;
import SaleManagement.VinhNguyen.exception.ErrorCode;
import SaleManagement.VinhNguyen.mapper.UserMapper;
import SaleManagement.VinhNguyen.repository.RefreshTokenRepository;
import SaleManagement.VinhNguyen.repository.UserRepository;
import SaleManagement.VinhNguyen.request.ChangePasswordRequest;
import SaleManagement.VinhNguyen.request.UpdateProfileRequest;
import SaleManagement.VinhNguyen.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final Map<String, String> forgotPasswordOtpStorage = new ConcurrentHashMap<>();

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void toggleUserStatus(Long id, boolean enabled) {
        String currentAdminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        //cannot delete admin
        if (!enabled && user.getEmail().equals(currentAdminEmail)) {
            throw new AppException(ErrorCode.CANNOT_LOCK_YOURSELF);
        }
        // update new status
        user.setEnabled(enabled);

        //block user and delete refresh token if status=false
        if (!enabled && user.getRefreshTokens() != null) {
            // thanks to orphanRemoval = true
            user.getRefreshTokens().clear();
        }

        // save user status
        userRepository.save(user);
    }

    public String forgotPassword(String email) {
        // Tìm kiếm tài khoản dựa trên Email người dùng nhập
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra xem trạng thái tài khoản có bị khóa hay không
        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        // Tạo mã số OTP ngẫu nhiên gồm 6 ký tự số
        String otp = String.format("%06d", new Random().nextInt(1000000));

        // Lưu mã OTP vào bộ nhớ tạm thời trên RAM
        forgotPasswordOtpStorage.put(email, otp);

        // Tiến hành gửi Email chứa mã xác nhận
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("[Sportify Style] Yêu cầu đặt lại mật khẩu");
            message.setText("Chào bạn,\n\nMã OTP để khôi phục mật khẩu tài khoản Sportify của bạn là: "
                    + otp + "\n\nMã có hiệu lực trong vòng 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.");
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            // Nếu hệ thống gặp sự cố gửi mail, lập tức thu hồi mã OTP trong bộ nhớ tạm để tránh rác hệ thống
            forgotPasswordOtpStorage.remove(email);
            throw new AppException(ErrorCode.NO_EMAIL);
        }

        return "Mã OTP khôi phục mật khẩu đã được gửi đến Email của bạn.";
    }


    @jakarta.transaction.Transactional
    public String resetPassword(String email, String userInputOtp, String newPassword) {
        // Lấy mã OTP thực tế được lưu trữ trên hệ thống ra đối chiếu
        String serverOtp = forgotPasswordOtpStorage.get(email);

        // Kiểm tra tính hợp lệ của OTP (không tồn tại hoặc gõ sai)
        if (serverOtp == null || !serverOtp.equals(userInputOtp)) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        // Lấy đối tượng User từ Database ra để cập nhật dữ liệu mật khẩu mới
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Thực hiện băm (Mã hóa) mật khẩu mới bằng BCrypt trước khi lưu xuống SQL Server
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Hủy bỏ mã OTP cũ ra khỏi bộ nhớ tạm sau khi đã đổi thành công nhằm bảo mật
        forgotPasswordOtpStorage.remove(email);

        // BẢO MẬT: Xóa toàn bộ Refresh Token của User này trong DB.
        // Ép tất cả các thiết bị đang đăng nhập bằng tài khoản này (nếu có) phải đăng xuất ngay lập tức
        refreshTokenRepository.deleteByUser(user);

        return "Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại bằng mật khẩu mới.";
    }

    @Transactional
    public String changePassword(String email, ChangePasswordRequest changePasswordRequest){
        User user=userRepository.findByEmail(email).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if(!passwordEncoder.matches(changePasswordRequest.getOldPassword(),user.getPassword())){
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }
        if(!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getNewPasswordAgain())){
            throw new AppException(ErrorCode.NEW_PASS_AGAIN_EQUAL_NEW_PASS);
        }
        String encodedPassword=passwordEncoder.encode(changePasswordRequest.getNewPassword());
        user.setPassword(encodedPassword);
        return "Change password successfully";
    }

    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setFullName(request.getFullName());
        return UserMapper.toResponse(user);
    }

}