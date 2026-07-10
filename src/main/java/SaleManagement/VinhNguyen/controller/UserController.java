package SaleManagement.VinhNguyen.controller;

import SaleManagement.VinhNguyen.response.UserResponse;
import SaleManagement.VinhNguyen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}