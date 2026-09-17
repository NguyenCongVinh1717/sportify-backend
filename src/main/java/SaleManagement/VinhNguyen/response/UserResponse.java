package SaleManagement.VinhNguyen.response;

import SaleManagement.VinhNguyen.enums.Role;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private Boolean enabled;
}