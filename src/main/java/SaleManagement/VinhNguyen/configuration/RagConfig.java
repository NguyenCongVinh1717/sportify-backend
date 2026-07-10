package SaleManagement.VinhNguyen.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagConfig {

    // ✨ THÊM MỚI: Kho vector trong bộ nhớ (RAM) — mất dữ liệu khi restart app,
    // nên cần gọi lại /ai/reindex sau mỗi lần khởi động (xem ProductIndexingService).
    // Nếu sau này muốn lưu bền vững, thay bằng Redis/PGVector/Chroma... (đổi bean này,
    // phần còn lại của tính năng RAG không cần sửa gì).
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    // ✨ THÊM MỚI: ChatClient dùng chung cho toàn bộ tính năng tư vấn AI,
    // có sẵn "system prompt" định hướng AI chỉ tư vấn dựa trên dữ liệu sản phẩm thật.
    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder
                .defaultSystem("""
                        Bạn là trợ lý tư vấn bán hàng của cửa hàng thời trang thể thao Sportify.
                        Chỉ được tư vấn dựa trên thông tin sản phẩm nằm trong phần ngữ cảnh (context) được cung cấp.
                        Nếu ngữ cảnh không có sản phẩm phù hợp với câu hỏi, hãy trả lời trung thực là hiện chưa có
                        sản phẩm phù hợp, KHÔNG được bịa ra sản phẩm không tồn tại.
                        Luôn trả lời ngắn gọn, thân thiện, dễ hiểu, bằng tiếng Việt.
                        Khi gợi ý sản phẩm, nêu rõ tên sản phẩm, giá, và các lựa chọn size/màu nếu có trong ngữ cảnh.
                        """)
                .build();
    }
}