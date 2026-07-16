package SaleManagement.VinhNguyen.controller;

import SaleManagement.VinhNguyen.request.ChangePasswordRequest;
import SaleManagement.VinhNguyen.request.ForgotPasswordRequest;
import SaleManagement.VinhNguyen.request.ResetPasswordRequest;
import SaleManagement.VinhNguyen.request.UpdateProfileRequest;
import SaleManagement.VinhNguyen.response.UserResponse;
import SaleManagement.VinhNguyen.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/{id}/status")
    public String changeUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled) {

        userService.toggleUserStatus(id, enabled);

        return enabled ? "Mở khóa tài khoản thành công!" : "Khóa tài khoản thành công!";
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        String message = userService.forgotPassword(request.getEmail().trim());
        return Map.of("message", message);
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        String message = userService.resetPassword(
                request.getEmail().trim(),
                request.getOtp().trim(),
                request.getNewPassword().trim()
        );
        return Map.of("message", message);
    }

    @PostMapping("/change-password")
    public Map<String, String> changePassword(Principal principal, @RequestBody @Valid ChangePasswordRequest request) {
        String message = userService.changePassword(
                principal.getName(),
                request
        );
        return Map.of("message", message);
    }

    @PostMapping("/update-profile")
    public UserResponse updateProfile(Principal principal, @RequestBody @Valid UpdateProfileRequest request) {
        return  userService.updateProfile(
                principal.getName(),
                request
        );
    }
}