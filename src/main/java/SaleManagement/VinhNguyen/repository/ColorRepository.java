package SaleManagement.VinhNguyen.repository;

import SaleManagement.VinhNguyen.entity.Color;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {
    boolean existsByColorCode(String colorCode);
}