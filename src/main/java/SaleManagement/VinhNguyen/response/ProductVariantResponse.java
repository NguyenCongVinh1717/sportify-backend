package SaleManagement.VinhNguyen.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductVariantResponse {
    private Long id;
    private Long colorId;
    private String colorCode;
    private String colorName;
    private Long sizeId;
    private String sizeCode;
    private String sizeName;
    private int stock;
}