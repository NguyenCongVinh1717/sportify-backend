package SaleManagement.VinhNguyen.service;

import SaleManagement.VinhNguyen.entity.RefreshToken;
import SaleManagement.VinhNguyen.entity.User;
import SaleManagement.VinhNguyen.enums.Role;
import SaleManagement.VinhNguyen.exception.AppException;
import SaleManagement.VinhNguyen.exception.ErrorCode;
import SaleManagement.VinhNguyen.repository.RefreshTokenRepository;
import SaleManagement.VinhNguyen.repository.UserRepository;
import SaleManagement.VinhNguyen.request.ChangePasswordRequest;
import SaleManagement.VinhNguyen.request.LoginRequest;
import SaleManagement.VinhNguyen.request.RegisterRequest;
import SaleManagement.VinhNguyen.response.AuthResponse;
import SaleManagement.VinhNguyen.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage; // THÊM MỚI
import org.springframework.mail.javamail.JavaMailSender; // THÊM MỚI
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.stringtemplate.v4.ST;

import java.util.Map; // THÊM MỚI
import java.util.Random; // THÊM MỚI
import java.util.concurrent.ConcurrentHashMap; // THÊM MỚI

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginAttemptService loginAttemptService;

    // THÊM MỚI: Bộ nhớ tạm thời lưu thông tin đăng ký và OTP (Tự giải phóng sau khi xác thực xong)
    private final Map<String, RegisterRequest> pendingRegistrations = new ConcurrentHashMap<>();
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    // Lưu OTP quên mật khẩu (Key: Email, Value: Mã OTP 6 số)
    private final Map<String, String> forgotPasswordOtpStorage = new ConcurrentHashMap<>();

    public AuthResponse login(LoginRequest request){

        if(loginAttemptService.isBlocked(request.getEmail())){
            throw new AppException(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS);
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->{
                    loginAttemptService.loginFailed(request.getEmail());
                    return new AppException(ErrorCode.USER_NOT_FOUND);
                });

        if(!user.getEnabled()){
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        if(!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )){
            loginAttemptService.loginFailed(request.getEmail());
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }
        loginAttemptService.loginSucceeded(request.getEmail());

        String accessToken = jwtService.generateToken(user);
        String refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String register(RegisterRequest request){

        boolean existed = userRepository
                .existsByEmail(request.getEmail());

        if(existed){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // create OTP 6 digits
        String otp = String.format("%06d", new Random().nextInt(1000000));

        // 2. Lưu thông tin người dùng gõ vào bộ nhớ tạm thời
        pendingRegistrations.put(request.getEmail(), request);
        otpStorage.put(request.getEmail(), otp);

        // 3. Thực hiện gửi Email chứa mã số OTP về Gmail thật của khách
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getEmail());
            message.setSubject("[Sportify Style] Mã kích hoạt tài khoản thành viên");
            message.setText("Chào bạn,\n\nMã OTP để xác thực đăng ký tài khoản của bạn tại Sportify là: "
                    + otp + "\n\nMã có hiệu lực trong vòng 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.");
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            // Nếu gửi mail lỗi, xóa dữ liệu tạm ngay để tránh rác RAM
            pendingRegistrations.remove(request.getEmail());
            otpStorage.remove(request.getEmail());
            throw new AppException(ErrorCode.NO_EMAIL);
        }

        return "Mã OTP đã được gửi thành công.";
    }

    //Hàm kiểm tra mã OTP
    public AuthResponse verifyOtp(String email, String userInputOtp) {
        String serverOtp = otpStorage.get(email);
        RegisterRequest request = pendingRegistrations.get(email);

        // Kiểm tra tính hợp lệ của mã OTP
        if (serverOtp == null || !serverOtp.equals(userInputOtp)) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        // Mã hóa mật khẩu trước khi lưu xuống DB
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(encodedPassword)
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        // CHÍNH THỨC LƯU VÀO DATABASE
        userRepository.save(user);

        // Xóa sạch dữ liệu trong bộ nhớ tạm sau khi kích hoạt thành công
        otpStorage.remove(email);
        pendingRegistrations.remove(email);

        // Tự động cấp luôn Token đăng nhập để Frontend lưu session vào thẳng Store luôn
        String accessToken = jwtService.generateToken(user);
        String refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    public AuthResponse loginWithGoogle(String googleTokenString) {
        String email;
        String fullName;

        try {
            com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier verifier =
                    new com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder(
                            new com.google.api.client.http.javanet.NetHttpTransport(),
                            new com.google.api.client.json.gson.GsonFactory()
                    )
                            .setAudience(java.util.Collections.singletonList("1093888167494-njc36m9n4lhbeess9fskjeork1ks3in2.apps.googleusercontent.com"))
                            .build();

            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken idToken = verifier.verify(googleTokenString);
            if (idToken == null) {
                throw new AppException(ErrorCode.INVALID_GOOGLE_TOKEN);
            }

            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload = idToken.getPayload();
            email = payload.getEmail();
            fullName = (String) payload.get("name");

        } catch (AppException ae) {
            throw ae;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xác thực Google: " + e.getMessage());
        }


        // Tìm kiếm tài khoản trong Database
        User user = userRepository.findByEmail(email).orElse(null);

        // Nếu tài khoản chưa từng tồn tại -> Tiến hành tự động đăng ký
        if (user == null) {
            user = User.builder()
                    .email(email)
                    .fullName(fullName)
                    .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                    .role(Role.ROLE_USER)
                    .enabled(true)
                    .build();

            user = userRepository.save(user);
        }

        //Kiểm tra nếu tài khoản đang bị khóa vĩnh viễn
        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        // generate Token JWT
        String accessToken = jwtService.generateToken(user);
        String refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    @Transactional
    public String createRefreshToken(User user) {
        //xoá refresh token cũ
        refreshTokenRepository.deleteByUser(user);
        // Mỗi lần đăng nhập, tạo 1 token mới.
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(java.util.UUID.randomUUID().toString())
                .expiryDate(java.time.Instant.now().plus(7, java.time.temporal.ChronoUnit.DAYS))
                .build();
        return refreshTokenRepository.save(refreshToken).getToken();
    }


    public AuthResponse refreshAccessToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        // Kiểm tra hết hạn
        if (refreshToken.getExpiryDate().isBefore(java.time.Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(user);
        // Tùy chọn: Mày có thể xoay vòng refresh token (tạo mới luôn cả refresh token cho bảo mật)
        String newRefreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public AuthResponse logout(String refreshToken) {
        if (refreshToken != null) {
             refreshTokenRepository.deleteByToken(refreshToken);
        }

        return AuthResponse.builder()
                .userId(null)
                .email(null)
                .fullName(null)
                .role(null)
                .accessToken(null)
                .refreshToken(null)
                .build();
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


    @Transactional
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
    public String changePassword(String email,ChangePasswordRequest changePasswordRequest){
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
}