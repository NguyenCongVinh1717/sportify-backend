package SaleManagement.VinhNguyen.controller;

import SaleManagement.VinhNguyen.exception.AppException;
import SaleManagement.VinhNguyen.exception.ErrorCode;
import SaleManagement.VinhNguyen.request.*;
import SaleManagement.VinhNguyen.response.AuthResponse;
import SaleManagement.VinhNguyen.service.AuthService;
import jakarta.servlet.http.Cookie; // THÊM MỚI
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse; // THÊM MỚI
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        createAuthCookie(response, authResponse.getRefreshToken());
        return authResponse;
    }


    @PostMapping("/google")
    public AuthResponse loginWithGoogle(@RequestBody Map<String, String> request, HttpServletResponse response) {
        String googleToken = request.get("token");
        AuthResponse authResponse = authService.loginWithGoogle(googleToken);

        createAuthCookie(response, authResponse.getRefreshToken());

        return authResponse;
    }

    @PostMapping("/register")
    public Map<String, String> register(
            @RequestBody @Valid RegisterRequest request
    ){
        String message = authService.register(request);
        return Map.of("message", message);
    }

    @PostMapping("/verify-otp")
    public AuthResponse verifyOtp(
            @RequestParam String email,
            @RequestParam String otp,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.verifyOtp(email, otp);

        createAuthCookie(response, authResponse.getRefreshToken());

        return authResponse;
    }

    private void createAuthCookie(HttpServletResponse response, String refreshToken) {
        // 7 days
        long maxAgeInSeconds = 7 * 24 * 60 * 60;

        String cookieHeader = String.format(
                "refreshToken=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                refreshToken,
                maxAgeInSeconds
        );
        response.setHeader("Set-Cookie", cookieHeader);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;

        // find in cookies
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        // request for new access token
        AuthResponse authResponse = authService.refreshAccessToken(refreshToken);
        // Update new refresh token for cookie
        createAuthCookie(response, authResponse.getRefreshToken());

        return authResponse;
    }

    @PostMapping("/logout")
    public AuthResponse logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;

        // find refresh cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        AuthResponse authResponse = authService.logout(refreshToken);

        // Delete cookie on browser
        String cookieHeader = "refreshToken=; Path=/; Max-Age=0; HttpOnly; Secure; SameSite=None";
        response.setHeader("Set-Cookie", cookieHeader);
        return authResponse;
    }

}