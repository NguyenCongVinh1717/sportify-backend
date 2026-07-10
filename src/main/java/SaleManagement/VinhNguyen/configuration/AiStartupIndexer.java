package SaleManagement.VinhNguyen.configuration;

import SaleManagement.VinhNguyen.service.ProductIndexingService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// ✨ THÊM MỚI: Tự động lập chỉ mục sản phẩm vào VectorStore ngay khi backend khởi động,
// để không phải gọi tay POST /ai/reindex mỗi lần chạy lại app.
// Nếu số lượng sản phẩm lớn, có thể cân nhắc bỏ @Component này và chỉ gọi /ai/reindex thủ công
// (vì mỗi lần index sẽ tốn lượt gọi API embedding tới Google GenAI).
@Component
@RequiredArgsConstructor
public class AiStartupIndexer implements ApplicationRunner {

    private final ProductIndexingService productIndexingService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            productIndexingService.reindexAllProducts();
            System.out.println(">>> [AI] Đã lập chỉ mục sản phẩm cho tính năng tư vấn RAG");
        } catch (Exception e) {
            System.err.println(">>> [AI] Lỗi khi lập chỉ mục sản phẩm: " + e.getMessage());
        }
    }
}