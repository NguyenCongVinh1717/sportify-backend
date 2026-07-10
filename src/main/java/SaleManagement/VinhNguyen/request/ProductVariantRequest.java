package SaleManagement.VinhNguyen.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductVariantRequest {

    @NotNull(message = "Color id is required")
    private Long colorId;

    @NotNull(message = "Size id is required")
    private Long sizeId;

    @Min(value = 0, message = "Stock must be greater than or equal 0")
    private int stock;
}