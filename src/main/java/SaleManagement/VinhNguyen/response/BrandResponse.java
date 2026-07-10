package SaleManagement.VinhNguyen.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BrandResponse {
    private Long id;
    private String brandCode;
    private String brandName;
}
