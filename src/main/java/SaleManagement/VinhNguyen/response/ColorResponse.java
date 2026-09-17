package SaleManagement.VinhNguyen.response;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ColorResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String colorCode;

    private String colorName;
}