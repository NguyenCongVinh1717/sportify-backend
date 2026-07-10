package SaleManagement.VinhNguyen.response;

import SaleManagement.VinhNguyen.enums.Role;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private Boolean enabled;
}