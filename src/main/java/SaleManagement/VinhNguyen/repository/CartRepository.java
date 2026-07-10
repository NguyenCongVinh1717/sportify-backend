package SaleManagement.VinhNguyen.repository;

import SaleManagement.VinhNguyen.entity.Cart;
import SaleManagement.VinhNguyen.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart,Long> {
    Optional<Cart> findByUser(User user);
}
