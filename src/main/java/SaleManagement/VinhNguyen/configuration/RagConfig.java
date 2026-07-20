package SaleManagement.VinhNguyen.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;

@Configuration
public class RagConfig {

    @Bean
    @Primary
    public VectorStore textVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(768)
                .vectorTableName("vector_store_text")
                .build();
    }

    @Bean(name = "imageVectorStore")
    public VectorStore imageVectorStore(JdbcTemplate jdbcTemplate) {
        // Triển khai đầy đủ cả 2 hàm trừu tượng bắt buộc của phiên bản 2.0.0
        EmbeddingModel fakeEmbeddingModel = new EmbeddingModel() {
            @Override
            public EmbeddingResponse call(EmbeddingRequest request) {
                return new EmbeddingResponse(Collections.emptyList());
            }

            @Override
            public float[] embed(Document document) {
                return new float[0];
            }
        };

        return PgVectorStore.builder(jdbcTemplate, fakeEmbeddingModel)
                .dimensions(512)
                .vectorTableName("vector_store_image")
                .build();
    }

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