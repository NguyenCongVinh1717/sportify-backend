package SaleManagement.VinhNguyen.response;

import SaleManagement.VinhNguyen.entity.Image;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductResponse {
    private Long id;
    private String productCode;
    private String productName;
    private double price;
    private int quantity;
    private Long brandId;
    private String brandCode;
    private String brandName;
    private List<String> images;
    private List<ProductVariantResponse> variants;
}
