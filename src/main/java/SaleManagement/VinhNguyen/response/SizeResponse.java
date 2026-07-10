package SaleManagement.VinhNguyen.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SizeResponse {

    private Long id;

    private String sizeCode;

    private String sizeName;
}