package SaleManagement.VinhNguyen.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SizeRequest {

    @NotBlank(message = "Size code is required")
    private String sizeCode;

    @NotBlank(message = "Size name is required")
    private String sizeName;
}