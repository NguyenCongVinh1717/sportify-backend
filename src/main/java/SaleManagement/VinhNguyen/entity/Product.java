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
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String productCode;
    private String productName;
    private double price;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "brand_id",nullable = false)
    private Brand brand;

    @OneToMany(orphanRemoval = true,mappedBy = "product",cascade = CascadeType.ALL)
    List<Image> images;

    @OneToMany(orphanRemoval = true,mappedBy = "product",cascade = CascadeType.ALL)
    private List<ProductColorSize> productVariants;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

}
