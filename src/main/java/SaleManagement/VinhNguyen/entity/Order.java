package SaleManagement.VinhNguyen.entity;

import SaleManagement.VinhNguyen.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double totalPrice;
    private String receiver;
    private String address;
    private String phone;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String paymentMethod;
    @ManyToOne(optional = true,fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;
    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<OrderItem> orderItems;
}
