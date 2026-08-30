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

@Service
@RequiredArgsConstructor
public class ProductIndexingService {

    private final ProductRepository productRepository;
    // ✨ ĐỔI TÊN: vectorStore -> textVectorStore để đồng bộ với cấu hình mới
    private final VectorStore textVectorStore;

    public void reindexAllProducts() {
        List<Product> products = productRepository.findAllWithDetails();

        List<Document> documents = products.stream()
                .map(this::toDocument)
                .collect(Collectors.toList());

        textVectorStore.add(documents); // ✨ Đổi sang textVectorStore để lưu bền vững vào bảng Postgres
    }

    private Document toDocument(Product p) {
        String brandName = p.getBrand() != null ? p.getBrand().getBrandName() : "Đang cập nhật";
        String sizes = "Đang cập nhật";
        String colors = "Đang cập nhật";

        List<ProductColorSize> variants = p.getProductVariants();
        if (variants != null && !variants.isEmpty()) {
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
                "Sản phẩm: %s%nThương hiệu: %s%nGiá: %,.0f đ%nKích cỡ có sẵn: %s%nMàu sắc có sẵn: %s%nMô tả: %s",
                p.getProductName(), brandName, p.getPrice(), sizes, colors, p.getDescription() != null ? p.getDescription() : "Đang cập nhật"
        );

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("productId", p.getId());
        metadata.put("productName", p.getProductName());
        metadata.put("price", p.getPrice());
        metadata.put("description", p.getDescription()); // Add description to metadata

        return new Document(content, metadata);
    }
}