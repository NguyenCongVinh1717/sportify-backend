package SaleManagement.VinhNguyen.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int rating;
    private String content;

    @ManyToOne(fetch = FetchType.LAZY,optional = true)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY,optional = true)
    @JoinColumn(name = "product_id",nullable = false)
    private Product product;

}
