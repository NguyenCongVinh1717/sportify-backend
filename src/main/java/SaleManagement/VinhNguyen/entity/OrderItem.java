package SaleManagement.VinhNguyen.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double price;
    private int quantity;

    private Long productIdSnapshot;
    private String productNameSnapshot;
    private String colorSnapshot;
    private String sizeSnapshot;
    private String imageUrlSnapshot;

    @ManyToOne(fetch = FetchType.LAZY,optional = true)
    @JoinColumn(name = "product_color_size_id",nullable = false)
    private ProductColorSize productColorSize;
    @ManyToOne(fetch = FetchType.LAZY,optional = true)
    @JoinColumn(name = "order_id",nullable = false)
    private Order order;
}
