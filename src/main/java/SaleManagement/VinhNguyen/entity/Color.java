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
public class Color {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String colorCode;
    private String colorName;
    @OneToMany(orphanRemoval = true,mappedBy = "color",cascade = CascadeType.ALL)
    private List<ProductColorSize> productVariants;
}
