package SaleManagement.VinhNguyen.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "The pattern of email is invalid")
    private String email;

    @NotBlank(message = "Fullname is required")
    @Size(min = 2, max = 50, message = "Fullname must be from 2 to 50 digits")
    private String fullName;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password has to be at least 8 digits")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one number"
    )
    private String password;
}