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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("test@gmail.com")
                .fullName("Nguyen Van A")
                .password("encoded_password")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@gmail.com");
        loginRequest.setPassword("raw_password");

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@gmail.com");
        registerRequest.setFullName("User Moi");
        registerRequest.setPassword("Password123@");
    }

    // =========================================================================
    // TC-AUTH-01: Đăng ký hợp lệ -> Gửi OTP
    // =========================================================================
    @Test
    @DisplayName("TC-AUTH-01: Đăng ký email mới hợp lệ -> Trả về thông báo thành công và gửi mail")
    void register_Success() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        String result = authService.register(registerRequest);

        assertEquals("Mã OTP đã được gửi thành công.", result);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    // =========================================================================
    // TC-AUTH-02: Đăng ký email đã tồn tại -> USER_EXISTED
    // =========================================================================
    @Test
    @DisplayName("TC-AUTH-02: Đăng ký email đã tồn tại -> Ném lỗi USER_EXISTED")
    void register_EmailExisted_ThrowsException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> authService.register(registerRequest));
        assertEquals(ErrorCode.USER_EXISTED, ex.getErrorCode());
    }

    // =========================================================================
    // TC-AUTH-03 & 04: Xác thực OTP (Đúng & Sai)
    // =========================================================================
    @Test
    @DisplayName("TC-AUTH-03: Xác thực OTP đúng -> Lưu DB và trả về AuthResponse")
    void verifyOtp_Success() {
        // Step 1: Gọi register để lưu vào memory tạm
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        authService.register(registerRequest);

        // Capture lại mã OTP được gửi đi trong Mail
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        String text = messageCaptor.getValue().getText();
        String sentOtp = text.substring(text.indexOf("là: ") + 4, text.indexOf("\n\nMã có")).trim();

        // Step 2: Giả lập lưu DB & Token
        when(passwordEncoder.encode(any())).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtService.generateToken(any())).thenReturn("access_token");
        when(refreshTokenRepository.save(any())).thenReturn(RefreshToken.builder().token("refresh_token").build());

        AuthResponse response = authService.verifyOtp(registerRequest.getEmail(), sentOtp);

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("TC-AUTH-04: Xác thực OTP sai -> Ném lỗi INVALID_OTP")
    void verifyOtp_WrongOtp_ThrowsException() {
        AppException ex = assertThrows(AppException.class, () -> authService.verifyOtp("test@gmail.com", "999999"));
        assertEquals(ErrorCode.INVALID_OTP, ex.getErrorCode());
    }

    // =========================================================================
    // TC-AUTH-05: Đăng nhập thành công
    // =========================================================================
    @Test
    @DisplayName("TC-AUTH-05: Đăng nhập đúng thông tin -> Trả về Token và reset bộ đếm sai")
    void login_Success() {
        when(loginAttemptService.isBlocked(loginRequest.getEmail())).thenReturn(false);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("raw_password", "encoded_password")).thenReturn(true);
        when(jwtService.generateToken(sampleUser)).thenReturn("access_token_123");
        when(refreshTokenRepository.save(any())).thenReturn(RefreshToken.builder().token("refresh_token_123").build());

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("access_token_123", response.getAccessToken());
        verify(loginAttemptService).loginSucceeded(loginRequest.getEmail());
    }

    // =========================================================================
    // TC-AUTH-06: Đăng nhập sai mật khẩu -> WRONG_PASSWORD
    // =========================================================================
    @Test
    @DisplayName("TC-AUTH-06: Đăng nhập sai mật khẩu -> Tăng bộ đếm và ném lỗi WRONG_PASSWORD")
    void login_WrongPassword_ThrowsException() {
        when(loginAttemptService.isBlocked(loginRequest.getEmail())).thenReturn(false);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("raw_password", "encoded_password")).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> authService.login(loginRequest));

        assertEquals(ErrorCode.WRONG_PASSWORD, ex.getErrorCode());
        verify(loginAttemptService).loginFailed(loginRequest.getEmail());
    }

    // =========================================================================
    // TC-AUTH-07: Bị khóa do thử quá nhiều lần -> TOO_MANY_LOGIN_ATTEMPTS
    // =========================================================================
    @Test
    @DisplayName("TC-AUTH-07: Đã bị khóa do Brute-force -> Trả về lỗi TOO_MANY_LOGIN_ATTEMPTS")
    void login_Blocked_ThrowsException() {
        when(loginAttemptService.isBlocked(loginRequest.getEmail())).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> authService.login(loginRequest));
        assertEquals(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS, ex.getErrorCode());
    }

    // =========================================================================
    // TC-AUTH-08: Tài khoản bị Admin vô hiệu hóa -> ACCOUNT_DISABLED
    // =========================================================================
    @Test
    @DisplayName("TC-AUTH-08: Đăng nhập khi enabled=false -> Ném lỗi ACCOUNT_DISABLED")
    void login_DisabledAccount_ThrowsException() {
        sampleUser.setEnabled(false);
        when(loginAttemptService.isBlocked(loginRequest.getEmail())).thenReturn(false);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(sampleUser));

        AppException ex = assertThrows(AppException.class, () -> authService.login(loginRequest));
        assertEquals(ErrorCode.ACCOUNT_DISABLED, ex.getErrorCode());
    }

    // =========================================================================
    // TC-AUTH-11: Refresh Token hết hạn hoặc không tồn tại
    // =========================================================================
    @Test
    @DisplayName("TC-AUTH-11a: Refresh Token hợp lệ -> Cấp Access Token mới")
    void refreshAccessToken_Success() {
        RefreshToken token = RefreshToken.builder()
                .token("valid_rf_token")
                .user(sampleUser)
                .expiryDate(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();

        when(refreshTokenRepository.findByToken("valid_rf_token")).thenReturn(Optional.of(token));
        when(jwtService.generateToken(sampleUser)).thenReturn("new_access_token");
        when(refreshTokenRepository.save(any())).thenReturn(token);

        AuthResponse response = authService.refreshAccessToken("valid_rf_token");

        assertNotNull(response);
        assertEquals("new_access_token", response.getAccessToken());
    }

    @Test
    @DisplayName("TC-AUTH-11b: Refresh Token đã hết hạn -> Ném lỗi TOKEN_EXPIRED và xóa token")
    void refreshAccessToken_Expired_ThrowsException() {
        RefreshToken token = RefreshToken.builder()
                .token("expired_rf_token")
                .user(sampleUser)
                .expiryDate(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();

        when(refreshTokenRepository.findByToken("expired_rf_token")).thenReturn(Optional.of(token));

        AppException ex = assertThrows(AppException.class, () -> authService.refreshAccessToken("expired_rf_token"));

        assertEquals(ErrorCode.TOKEN_EXPIRED, ex.getErrorCode());
        verify(refreshTokenRepository).delete(token);
    }

    // =========================================================================
    // TC-AUTH-20: Đăng xuất
    // =========================================================================
    @Test
    @DisplayName("TC-AUTH-20: Đăng xuất -> Xóa RefreshToken trong DB")
    void logout_Success() {
        AuthResponse response = authService.logout("some_refresh_token");

        verify(refreshTokenRepository, times(1)).deleteByToken("some_refresh_token");
        assertNull(response.getAccessToken());
    }

    // =========================================================================
    // TC-AUTH-21: Đăng nhập bằng Email chưa verify OTP (Chưa có trong DB)
    // =========================================================================
    @Test
    @DisplayName("TC-AUTH-21: Login bằng email chưa verify OTP (chưa tạo DB) -> USER_NOT_FOUND")
    void login_UnverifiedUser_ThrowsException() {
        when(loginAttemptService.isBlocked("unverified@gmail.com")).thenReturn(false);
        when(userRepository.findByEmail("unverified@gmail.com")).thenReturn(Optional.empty());

        loginRequest.setEmail("unverified@gmail.com");

        AppException ex = assertThrows(AppException.class, () -> authService.login(loginRequest));
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }
}