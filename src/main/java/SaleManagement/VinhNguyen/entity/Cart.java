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
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true,mappedBy = "cart")
    List<Cart_Product> cart_products;

    @OneToOne
    @JoinColumn(name = "user_id",unique = true)
    private User user;

}
