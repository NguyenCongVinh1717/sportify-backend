package SaleManagement.VinhNguyen.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ColorResponse {

    private Long id;

    private String colorCode;

    private String colorName;
}