package SaleManagement.VinhNguyen.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderItemResponse {
    private String productName;
    private String colorName;
    private String sizeName;
    private double price;
    private int quantity;
}
