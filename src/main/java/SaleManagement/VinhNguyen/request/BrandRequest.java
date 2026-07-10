package SaleManagement.VinhNguyen.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BrandRequest {
    @NotBlank(message = "Brand code is required")
    private String brandCode;
    @NotBlank(message = "Brand name is required")
    private String brandName;
}
