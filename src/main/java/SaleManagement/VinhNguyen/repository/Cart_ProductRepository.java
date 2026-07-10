package SaleManagement.VinhNguyen.repository;

import SaleManagement.VinhNguyen.entity.Cart;
import SaleManagement.VinhNguyen.entity.Cart_Product;
import SaleManagement.VinhNguyen.entity.Product;
import SaleManagement.VinhNguyen.entity.ProductColorSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Cart_ProductRepository extends JpaRepository<Cart_Product,Long> {
    Optional<Cart_Product> findByCartAndProductColorSize(Cart cart, ProductColorSize product);

    List<Cart_Product> findByCart(Cart cart);

//    void deleteByCartAndProduct(Cart cart, Product product);
}
