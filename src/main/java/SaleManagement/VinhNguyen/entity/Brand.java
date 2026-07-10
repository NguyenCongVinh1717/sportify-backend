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
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String brandCode;
    private String brandName;

    @OneToMany(mappedBy ="brand",orphanRemoval = true,cascade = CascadeType.ALL)
    List<Product> products;
}
