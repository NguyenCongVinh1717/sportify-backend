package SaleManagement.VinhNguyen.response;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BrandResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String brandCode;
    private String brandName;
}
