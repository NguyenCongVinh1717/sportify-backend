package SaleManagement.VinhNguyen.controller;

import SaleManagement.VinhNguyen.service.ProductIndexingService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiConsultController {

    private final ChatClient chatClient;
    // ✨ ĐỔI TÊN: vectorStore -> textVectorStore để nhận diện đúng bảng text trong Postgres
    private final VectorStore textVectorStore;
    private final ProductIndexingService productIndexingService;

    public record ConsultRequest(String question) {}
    public record ConsultResponse(String answer) {}

    @PostMapping("/consult")
    public ConsultResponse consult(@RequestBody ConsultRequest request) {
        Advisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.5)
                        .vectorStore(textVectorStore) // ✨ Đổi sang dùng textVectorStore ở đây
                        .build())
                .build();

        String answer = chatClient.prompt()
                .advisors(ragAdvisor)
                .user(request.question())
                .call()
                .content();

        return new ConsultResponse(answer);
    }

    @PostMapping("/reindex")
    public String reindex() {
        productIndexingService.reindexAllProducts();
        return "Đã lập chỉ mục lại toàn bộ sản phẩm vào CSDL PostgreSQL cho AI tư vấn";
    }
}