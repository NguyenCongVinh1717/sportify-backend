package SaleManagement.VinhNguyen.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ColorRequest {

    @NotBlank(message = "Color code is required")
    private String colorCode;

    @NotBlank(message = "Color name is required")
    private String colorName;
}