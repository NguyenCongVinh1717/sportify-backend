package SaleManagement.VinhNguyen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Size {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sizeCode;
    private String sizeName;
    @OneToMany(orphanRemoval = true,mappedBy = "size",cascade = CascadeType.ALL)
    private List<ProductColorSize> productVariants;
}
