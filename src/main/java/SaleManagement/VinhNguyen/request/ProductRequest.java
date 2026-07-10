package SaleManagement.VinhNguyen.request;

import SaleManagement.VinhNguyen.entity.Image;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductRequest {
    @NotBlank(message = "Product code is required")
    private String productCode;
    @NotBlank(message = "Product name is required")
    private String productName;
    @NotNull(message = "Price is required")
    private double price;
    @NotNull(message = "Brand Id is required")
    private Long brandId;

    private List<String> images;
    private List<ProductVariantRequest> variants;
}
