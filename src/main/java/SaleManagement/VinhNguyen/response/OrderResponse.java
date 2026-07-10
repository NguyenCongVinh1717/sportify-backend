package SaleManagement.VinhNguyen.response;

import lombok.*;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderResponse {
    private Long id;
    private double totalPrice;
    private String status;
    private String receiver;
    private String address;
    private String phone;
    private String paymentMethod;
    private List<OrderItemResponse> items;
    private String paymentUrl;
}