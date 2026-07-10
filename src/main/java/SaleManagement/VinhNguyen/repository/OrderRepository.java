package SaleManagement.VinhNguyen.repository;

import SaleManagement.VinhNguyen.entity.Order;
import SaleManagement.VinhNguyen.entity.User;
import SaleManagement.VinhNguyen.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findByStatusOrderByIdDesc(OrderStatus status);
    List<Order> findAllByOrderByIdDesc();
}
