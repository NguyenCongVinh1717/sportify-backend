package SaleManagement.VinhNguyen.repository;


import SaleManagement.VinhNguyen.entity.Brand;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand,Long> {
    boolean existsByBrandCode(@NotBlank(message = "Brand code is required") String brandCode);

    Brand findByBrandCode(@NotBlank(message = "Brand code is required") String brandCode);
}
