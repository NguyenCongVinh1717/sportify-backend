package SaleManagement.VinhNguyen.repository;

import SaleManagement.VinhNguyen.entity.Order;
import SaleManagement.VinhNguyen.entity.User;
import SaleManagement.VinhNguyen.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findAllByOrderByIdDesc();
    Page<Order> findAllByOrderByIdDesc(Pageable pageable);
    Page<Order> findByStatusOrderByIdDesc(OrderStatus status, Pageable pageable);
    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.orderItems item " +
            "WHERE o.user.id = :userId " +
            "AND item.productColorSize.product.id = :productId " +
            "AND o.status = :status")
    boolean existsByUserIdAndProductIdAndStatus(@Param("userId") Long userId,
                                                @Param("productId") Long productId,
                                                @Param("status") OrderStatus status);
}
