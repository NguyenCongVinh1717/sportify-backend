package SaleManagement.VinhNguyen.service;

import SaleManagement.VinhNguyen.entity.User;
import SaleManagement.VinhNguyen.exception.AppException;
import SaleManagement.VinhNguyen.exception.ErrorCode;
import SaleManagement.VinhNguyen.mapper.UserMapper;
import SaleManagement.VinhNguyen.repository.UserRepository;
import SaleManagement.VinhNguyen.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

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
}