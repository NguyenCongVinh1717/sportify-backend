package SaleManagement.VinhNguyen.repository;

import SaleManagement.VinhNguyen.entity.Product;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Kiểm tra trùng mã code: Chỉ tính những sản phẩm chưa bị xóa
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.productCode = :productCode AND p.isDeleted = false")
    boolean existsByProductCode(@Param("productCode") String productCode);

    // Lấy danh sách theo thương hiệu: Chỉ lấy sản phẩm chưa bị xóa
    List<Product> findByBrandIdAndIsDeletedFalse(Long brandId);

    // Tìm kiếm theo tên: Chỉ lấy sản phẩm chưa bị xóa
    Page<Product> findByProductNameContainingIgnoreCaseAndIsDeletedFalse(String keywords, Pageable pageable);

    // Lấy tất cả sản phẩm phân trang cho khách hàng: Chỉ lấy sản phẩm chưa bị xóa
    Page<Product> findByIsDeletedFalse(Pageable pageable);

    // Lấy sản phẩm theo thương hiệu phân trang: Chỉ lấy sản phẩm chưa bị xóa
    Page<Product> findByBrandIdAndIsDeletedFalse(Long brandId, Pageable pageable);

    // Tìm sản phẩm liên quan: Chỉ lấy sản phẩm chưa bị xóa
    @Query("SELECT p FROM Product p WHERE p.brand.id = :brandId AND p.id != :productId AND p.isDeleted = false")
    Page<Product> findRelatedProductsPaged(@Param("brandId") Long brandId,
                                           @Param("productId") Long productId,
                                           Pageable pageable);

    // Lọc theo giá: Chỉ lấy sản phẩm chưa bị xóa
    Page<Product> findByPriceLessThanEqualAndIsDeletedFalse(Double maxPrice, Pageable pageable);

    // Bộ lọc tổng hợp (Filter): Chỉ lọc các sản phẩm chưa bị xóa
    // và các biến thể chưa bị xóa (v.isDeleted = false)
    @Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN p.productVariants v
    WHERE p.isDeleted = false
      AND (v IS NULL OR v.isDeleted = false)
      AND (:brandId IS NULL OR p.brand.id = :brandId)
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

    // Dùng cho AI tư vấn: Chỉ lấy sản phẩm và các biến thể chưa bị xóa
    @Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN FETCH p.brand
    LEFT JOIN FETCH p.productVariants v
    LEFT JOIN FETCH v.color
    LEFT JOIN FETCH v.size
    WHERE p.isDeleted = false AND (v IS NULL OR v.isDeleted = false)
    """)
    List<Product> findAllWithDetails();

    @Query("SELECT DISTINCT p.productName FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.isDeleted = false")
    List<String> findSearchSuggestions(@Param("keyword") String keyword, Pageable pageable);
}