package SaleManagement.VinhNguyen.response;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SizeResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String sizeCode;

    private String sizeName;
}