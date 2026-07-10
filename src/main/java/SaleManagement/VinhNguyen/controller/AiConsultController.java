package SaleManagement.VinhNguyen.controller;

import SaleManagement.VinhNguyen.service.ProductIndexingService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

// ✨ THÊM MỚI: Controller cho tính năng "Tư vấn bằng AI (RAG)"
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiConsultController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ProductIndexingService productIndexingService;

    public record ConsultRequest(String question) {}
    public record ConsultResponse(String answer) {}

    /**
     * Frontend gọi: POST /ai/consult  body: { "question": "..." }
     * Luồng RAG: câu hỏi -> tìm các sản phẩm liên quan nhất trong VectorStore (Retrieve)
     * -> ghép vào prompt (Augment) -> gọi model sinh câu trả lời (Generate).
     */
    @PostMapping("/consult")
    public ConsultResponse consult(@RequestBody ConsultRequest request) {
        Advisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.5) // độ liên quan tối thiểu để 1 sản phẩm được đưa vào ngữ cảnh
                        .vectorStore(vectorStore)
                        .build())
                .build();

        String answer = chatClient.prompt()
                .advisors(ragAdvisor)
                .user(request.question())
                .call()
                .content();

        return new ConsultResponse(answer);
    }

    /**
     * Gọi tay để nạp/nạp lại toàn bộ sản phẩm vào VectorStore.
     * ⚠️ Vì SimpleVectorStore là bộ nhớ RAM (mất dữ liệu khi restart app),
     * cần gọi API này 1 lần sau mỗi lần khởi động backend, hoặc sau khi thêm nhiều sản phẩm mới.
     * Gợi ý: nên thêm bảo vệ quyền admin cho endpoint này trước khi đưa lên production.
     */
    @PostMapping("/reindex")
    public String reindex() {
        productIndexingService.reindexAllProducts();
        return "Đã lập chỉ mục lại toàn bộ sản phẩm cho AI tư vấn";
    }
}