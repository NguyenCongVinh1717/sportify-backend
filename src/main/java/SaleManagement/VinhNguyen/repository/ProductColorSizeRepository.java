package SaleManagement.VinhNguyen.repository;

import SaleManagement.VinhNguyen.entity.ProductColorSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductColorSizeRepository
        extends JpaRepository<ProductColorSize, Long> {
}