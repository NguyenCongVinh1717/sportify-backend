package SaleManagement.VinhNguyen.repository;

import SaleManagement.VinhNguyen.entity.Product;
import SaleManagement.VinhNguyen.response.ProductResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByProductCode(@NotBlank(message = "Product code is required") String productCode);

    List<Product> findByBrandId(Long brandId);

    Page<Product> findByProductNameContainingIgnoreCase(String keywords, Pageable pageable);

    Page<Product> findAll(Pageable pageable);

    Page<Product> findByBrandId(Long brandId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.brand.id = :brandId AND p.id != :productId")
    Page<Product> findRelatedProductsPaged(@Param("brandId") Long brandId,
                                           @Param("productId") Long productId,
                                           Pageable pageable);
    Page<Product> findByPriceLessThanEqual(Double maxPrice, Pageable pageable);

    @Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN p.productVariants v
    WHERE (:brandId IS NULL OR p.brand.id = :brandId)
      AND (:maxPrice IS NULL OR p.price <= :maxPrice)
      AND (:colorIds IS NULL OR v.color.id IN :colorIds)
      AND (:sizeIds IS NULL OR v.size.id IN :sizeIds)
    """)
    Page<Product> filterProducts(
            @Param("maxPrice") Double maxPrice,
            @Param("brandId") Long brandId,
            @Param("colorIds") List<Long> colorIds,
            @Param("sizeIds") List<Long> sizeIds,
            Pageable pageable
    );

    // THÊM MỚI: dùng cho tính năng AI tư vấn (ProductIndexingService)
// JOIN FETCH sẵn brand + productVariants + color + size để tránh LazyInitializationException
// khi ProductIndexingService xử lý dữ liệu ở bên ngoài transaction (ví dụ khi được gọi từ
// AiStartupIndexer lúc khởi động app).
    @Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN FETCH p.brand
    LEFT JOIN FETCH p.productVariants v
    LEFT JOIN FETCH v.color
    LEFT JOIN FETCH v.size
    """)
    List<Product> findAllWithDetails();
}
