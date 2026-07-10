package SaleManagement.VinhNguyen.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Cart_ProductResponse {
    private Long id;
    private Long productColorSizeId;
    private String productCode;
    private String productName;
    private String colorName;
    private String sizeName;
    private double price;
    private int quantity;
    private String image;
}
