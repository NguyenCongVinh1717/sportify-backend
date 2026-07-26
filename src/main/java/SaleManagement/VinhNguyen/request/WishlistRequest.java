package SaleManagement.VinhNguyen.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WishlistRequest{
    @NotNull(message = "Product id is required")
    private Long productId;

}