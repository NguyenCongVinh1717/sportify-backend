package SaleManagement.VinhNguyen.response;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CartResponse {
    private Long id;
    private List<Cart_ProductResponse> items;
}
