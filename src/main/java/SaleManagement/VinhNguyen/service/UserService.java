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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;

    private final Map<String, String> forgotPasswordOtpStorage = new ConcurrentHashMap<>();

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAllByOrderByIdDesc(pageable);
        return userPage.map(UserMapper::toResponse);
    }

    @Transactional
    public void toggleUserStatus(Long id, boolean enabled) {
        String currentAdminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // cannot delete admin
        if (!enabled && user.getEmail().equals(currentAdminEmail)) {
            throw new AppException(ErrorCode.CANNOT_LOCK_YOURSELF);
        }
        // update new status
        user.setEnabled(enabled);

        // block user and delete refresh token if status=false
        if (!enabled && user.getRefreshTokens() != null) {
            user.getRefreshTokens().clear();
        }

        userRepository.save(user);
    }

    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        String otp = String.format("%06d", new Random().nextInt(1000000));

        // Lưu mã OTP vào bộ nhớ tạm thời trên RAM
        forgotPasswordOtpStorage.put(email, otp);

        //Gọi hàm gửi mail bất đồng bộ (@Async) qua SendGrid từ EmailService
        emailService.sendOtpEmailAsync(email, otp);

        // Trả về kết quả tức thì cho Client
        return "Mã OTP khôi phục mật khẩu đã được gửi đến Email của bạn.";
    }

    @Transactional
    public String resetPassword(String email, String userInputOtp, String newPassword) {
        String serverOtp = forgotPasswordOtpStorage.get(email);

        if (serverOtp == null || !serverOtp.equals(userInputOtp)) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        forgotPasswordOtpStorage.remove(email);
        refreshTokenRepository.deleteByUser(user);

        return "Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại bằng mật khẩu mới.";
    }

    @Transactional
    public String changePassword(String email, ChangePasswordRequest changePasswordRequest){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if(!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())){
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }
        if(!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getNewPasswordAgain())){
            throw new AppException(ErrorCode.NEW_PASS_AGAIN_EQUAL_NEW_PASS);
        }
        String encodedPassword = passwordEncoder.encode(changePasswordRequest.getNewPassword());
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