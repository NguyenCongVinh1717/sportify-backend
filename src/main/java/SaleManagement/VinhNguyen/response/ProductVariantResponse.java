package SaleManagement.VinhNguyen.response;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductVariantResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long colorId;
    private String colorCode;
    private String colorName;
    private Long sizeId;
    private String sizeCode;
    private String sizeName;
    private int stock;
}