package SaleManagement.VinhNguyen.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AuthResponse {

    private Long userId;

    private String email;

    private String fullName;

    private String role;

    private String accessToken;

    //k có trong json trả về cho client
    @JsonIgnore
    private String refreshToken;
}