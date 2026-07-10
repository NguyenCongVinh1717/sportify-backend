package SaleManagement.VinhNguyen.mapper;

import SaleManagement.VinhNguyen.entity.User;
import SaleManagement.VinhNguyen.response.UserResponse;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .build();
    }
}