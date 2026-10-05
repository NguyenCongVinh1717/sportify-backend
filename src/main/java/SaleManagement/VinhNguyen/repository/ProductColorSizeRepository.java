package SaleManagement.VinhNguyen.repository;

import SaleManagement.VinhNguyen.entity.ProductColorSize;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductColorSizeRepository
        extends JpaRepository<ProductColorSize, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductColorSize p WHERE p.id = :id")
    Optional<ProductColorSize> findByIdWithLock(@Param("id") Long id);
}