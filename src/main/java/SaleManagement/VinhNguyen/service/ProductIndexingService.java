package SaleManagement.VinhNguyen.service;

import SaleManagement.VinhNguyen.entity.Product;
import SaleManagement.VinhNguyen.entity.ProductColorSize;
import SaleManagement.VinhNguyen.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// ✨ THÊM MỚI: Service lập chỉ mục (index) toàn bộ sản phẩm vào VectorStore để AI có thể "đọc" được
// dữ liệu sản phẩm thật khi tư vấn (thay vì chỉ trả lời chung chung từ kiến thức có sẵn của model).
@Service
@RequiredArgsConstructor
public class ProductIndexingService {

    private final ProductRepository productRepository;
    private final VectorStore vectorStore;

    /**
     * Đọc toàn bộ sản phẩm trong DB, biến mỗi sản phẩm thành 1 Document (đoạn văn bản mô tả),
     * rồi nạp vào VectorStore. Gọi hàm này:
     *  - 1 lần khi mới triển khai / sau khi thêm nhiều sản phẩm mới (qua endpoint POST /ai/reindex)
     *  - Lý tưởng nhất: gọi lại mỗi khi có sản phẩm được tạo/sửa/xóa (xem ghi chú cuối file)
     */
    public void reindexAllProducts() {
        List<Product> products = productRepository.findAllWithDetails();

        List<Document> documents = products.stream()
                .map(this::toDocument)
                .collect(Collectors.toList());

        vectorStore.add(documents);
    }

    private Document toDocument(Product p) {
        String brandName = p.getBrand() != null ? p.getBrand().getBrandName() : "Đang cập nhật";

        String sizes = "Đang cập nhật";
        String colors = "Đang cập nhật";

        List<ProductColorSize> variants = p.getProductVariants();
        if (variants != null && !variants.isEmpty()) {
            // ⚠️ LƯU Ý: mình giả định Size có getName() và Color có getColorName()
            // (khớp với ColorResponse/SizeResponse bạn đang dùng ở frontend: c.colorName, s.name).
            // Nếu entity Size/Color của bạn đặt tên getter khác, chỉ cần sửa 2 dòng bên dưới.
            sizes = variants.stream()
                    .filter(v -> v.getSize() != null)
                    .map(v -> v.getSize().getSizeName())
                    .distinct()
                    .collect(Collectors.joining(", "));

            colors = variants.stream()
                    .filter(v -> v.getColor() != null)
                    .map(v -> v.getColor().getColorName())
                    .distinct()
                    .collect(Collectors.joining(", "));
        }

        String content = String.format(
                "Sản phẩm: %s%nThương hiệu: %s%nGiá: %,.0f đ%nKích cỡ có sẵn: %s%nMàu sắc có sẵn: %s",
                p.getProductName(), brandName, p.getPrice(), sizes, colors
        );

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("productId", p.getId());
        metadata.put("productName", p.getProductName());
        metadata.put("price", p.getPrice());

        return new Document(content, metadata);
    }
}