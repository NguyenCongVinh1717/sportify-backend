package SaleManagement.VinhNguyen.service;

import SaleManagement.VinhNguyen.entity.User;
import SaleManagement.VinhNguyen.exception.AppException;
import SaleManagement.VinhNguyen.exception.ErrorCode;
import SaleManagement.VinhNguyen.mapper.UserMapper;
import SaleManagement.VinhNguyen.repository.RefreshTokenRepository;
import SaleManagement.VinhNguyen.repository.UserRepository;
import SaleManagement.VinhNguyen.request.ChangePasswordRequest;
import SaleManagement.VinhNguyen.request.UpdateProfileRequest;
import SaleManagement.VinhNguyen.response.CustomPageResponse;
import SaleManagement.VinhNguyen.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;

    // Inject StringRedisTemplate thay thế cho ConcurrentHashMap
    private final StringRedisTemplate redisTemplate;

    // Cache danh sách phân trang User an toàn bằng CustomPageResponse
    @Cacheable(value = "users_page", key = "{#pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
    public CustomPageResponse<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAllByOrderByIdDesc(pageable);
        return CustomPageResponse.fromPage(userPage.map(UserMapper::toResponse));
    }

    // Xóa toàn bộ cache danh sách khi thay đổi trạng thái User (Khóa/Mở khóa)
    @CacheEvict(value = "users_page", allEntries = true)
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

        // Lưu mã OTP quên mật khẩu vào Redis với thời hạn hết hạn (TTL) là 5 phút
        redisTemplate.opsForValue().set("FORGOT_OTP:" + email, otp, 5, TimeUnit.MINUTES);

        // Gọi hàm gửi mail bất đồng bộ
        emailService.sendOtpEmailAsync(email, otp);

        return "Mã OTP khôi phục mật khẩu đã được gửi đến Email của bạn.";
    }

    @Transactional
    public String resetPassword(String email, String userInputOtp, String newPassword) {
        // Lấy OTP từ Redis ra kiểm tra
        String serverOtp = redisTemplate.opsForValue().get("FORGOT_OTP:" + email);

        if (serverOtp == null || !serverOtp.equals(userInputOtp)) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Xóa OTP khỏi Redis ngay sau khi đổi mật khẩu thành công
        redisTemplate.delete("FORGOT_OTP:" + email);
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

    // Xóa cache danh sách khi thông tin cá nhân của User được cập nhật
    @CacheEvict(value = "users_page", allEntries = true)
    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setFullName(request.getFullName());
        return UserMapper.toResponse(user);
    }
}