package SaleManagement.VinhNguyen.repository;

import SaleManagement.VinhNguyen.entity.Size;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SizeRepository extends JpaRepository<Size, Long> {
    boolean existsBySizeCode(String sizeCode);
}