package SaleManagement.VinhNguyen.service;

import SaleManagement.VinhNguyen.entity.RefreshToken;
import SaleManagement.VinhNguyen.entity.User;
import SaleManagement.VinhNguyen.enums.Role;
import SaleManagement.VinhNguyen.exception.AppException;
import SaleManagement.VinhNguyen.exception.ErrorCode;
import SaleManagement.VinhNguyen.repository.RefreshTokenRepository;
import SaleManagement.VinhNguyen.repository.UserRepository;
import SaleManagement.VinhNguyen.request.LoginRequest;
import SaleManagement.VinhNguyen.request.RegisterRequest;
import SaleManagement.VinhNguyen.response.AuthResponse;
import SaleManagement.VinhNguyen.security.JwtService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginAttemptService loginAttemptService;
    private final EmailService emailService;

    // Inject Redis và ObjectMapper xử lý JSON
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

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

        boolean existed = userRepository.existsByEmail(request.getEmail());

        if(existed){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // 1. Tạo OTP 6 chữ số
        String otp = String.format("%06d", new Random().nextInt(1000000));

        try {
            // 2. Lưu thông tin đăng ký (chuyển sang JSON String) vào Redis với thời hạn (TTL) 5 phút
            String requestJson = objectMapper.writeValueAsString(request);
            redisTemplate.opsForValue().set("PENDING_REG:" + request.getEmail(), requestJson, 5, TimeUnit.MINUTES);

            // 3. Lưu OTP vào Redis với thời hạn (TTL) 5 phút
            redisTemplate.opsForValue().set("OTP:" + request.getEmail(), otp, 5, TimeUnit.MINUTES);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi mã hóa dữ liệu đăng ký", e);
        }

        // 4. Gọi hàm gửi mail chạy ngầm
        emailService.sendOtpEmailAsync(request.getEmail(), otp);

        return "Mã OTP đã được gửi thành công.";
    }

    // Hàm kiểm tra mã OTP từ Redis
    public AuthResponse verifyOtp(String email, String userInputOtp) {
        // Lấy OTP và Request tạm từ Redis
        String serverOtp = redisTemplate.opsForValue().get("OTP:" + email);
        String requestJson = redisTemplate.opsForValue().get("PENDING_REG:" + email);

        // Kiểm tra tính hợp lệ của mã OTP
        if (serverOtp == null || !serverOtp.equals(userInputOtp) || requestJson == null) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        RegisterRequest request;
        try {
            request = objectMapper.readValue(requestJson, RegisterRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Lỗi giải mã dữ liệu đăng ký", e);
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

        // Xóa sạch dữ liệu tạm trong Redis sau khi kích hoạt thành công
        redisTemplate.delete("OTP:" + email);
        redisTemplate.delete("PENDING_REG:" + email);

        // Tự động cấp luôn Token đăng nhập
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

        User user = userRepository.findByEmail(email).orElse(null);

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

        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

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
        refreshTokenRepository.deleteByUser(user);
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

        if (refreshToken.getExpiryDate().isBefore(java.time.Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(user);
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
}