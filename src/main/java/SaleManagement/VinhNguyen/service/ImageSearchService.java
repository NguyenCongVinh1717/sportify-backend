package SaleManagement.VinhNguyen.service;

import SaleManagement.VinhNguyen.entity.Image;
import SaleManagement.VinhNguyen.entity.Product;
import SaleManagement.VinhNguyen.mapper.ProductMapper;
import SaleManagement.VinhNguyen.repository.ProductRepository;
import SaleManagement.VinhNguyen.response.ProductResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.document.Document;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ImageSearchService {

    private final VectorStore imageVectorStore;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String PYTHON_SERVICE_URL = "http://127.0.0.1:8000/extract-image-vector";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private ProductRepository productRepository;

    public ImageSearchService(@Qualifier("imageVectorStore") VectorStore imageVectorStore) {
        this.imageVectorStore = imageVectorStore;
    }

    private List<Double> getVectorFromPython(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
        body.add("file", fileResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(PYTHON_SERVICE_URL, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (List<Double>) response.getBody().get("vector");
        }
        throw new RuntimeException("Không thể lấy vector đặc trưng từ ảnh");
    }

    public void indexProductImage(Long productId, String productName, MultipartFile file) throws IOException {
        List<Double> vector = getVectorFromPython(file);

        String id = java.util.UUID.randomUUID().toString();
        String content = "Sản phẩm: " + productName;
        Map<String, Object> metadata = Map.of("productId", productId, "productName", productName);

        String metadataJson = objectMapper.writeValueAsString(metadata);
        String vectorString = vector.toString(); // Dạng [v1, v2, v3...]

        // SỬA TẠI ĐÂY: Thêm ?::uuid cho vị trí của cột id
        String sql = "INSERT INTO public.vector_store_image (id, content, metadata, embedding) " +
                "VALUES (?::uuid, ?, ?::jsonb, ?::vector) " +
                "ON CONFLICT (id) DO UPDATE SET content = ?, metadata = ?::jsonb, embedding = ?::vector";

        // Thứ tự truyền tham số phải khớp chính xác với số lượng dấu hỏi (?)
        jdbcTemplate.update(sql,
                id, content, metadataJson, vectorString, // Cho phần VALUES
                content, metadataJson, vectorString      // Cho phần UPDATE
        );
    }

    public List<ProductResponse> searchByImage(MultipartFile file, int topK) throws IOException {
        List<Double> vector = getVectorFromPython(file);
        String vectorString = vector.toString();

        int candidateLimit = Math.max(topK, 20);

        String sql = """
        SELECT metadata, (embedding <=> ?::vector) as distance
        FROM public.vector_store_image
        ORDER BY distance ASC
        LIMIT ?
    """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, vectorString, candidateLimit);
        Map<Long, ProductResponse> uniqueResults = new java.util.LinkedHashMap<>();
        double maxAllowedDistance = 0.35;

        for (Map<String, Object> row : rows) {
            try {
                Object distanceObj = row.get("distance");
                double distance = distanceObj == null ? Double.MAX_VALUE : Double.parseDouble(distanceObj.toString());
                if (distance > maxAllowedDistance) {
                    continue;
                }

                Object metadataObj = row.get("metadata");
                if (metadataObj == null) continue;

                String metadataJson;
                if (metadataObj.getClass().getName().equals("org.postgresql.util.PGobject")) {
                    java.lang.reflect.Method method = metadataObj.getClass().getMethod("getValue");
                    metadataJson = (String) method.invoke(metadataObj);
                } else {
                    metadataJson = metadataObj.toString();
                }

                Map<String, Object> metaMap = objectMapper.readValue(metadataJson, Map.class);
                Object productIdObj = metaMap.get("productId");
                if (productIdObj != null) {
                    Long productId = Long.valueOf(productIdObj.toString());
                    productRepository.findById(productId).ifPresent(product -> {
                        uniqueResults.putIfAbsent(product.getId(), ProductMapper.toResponse(product));
                    });
                }
            } catch (Exception e) {
                System.err.println("Lỗi parse dữ liệu sản phẩm: " + e.getMessage());
            }
        }

        return new ArrayList<>(uniqueResults.values()).stream().limit(topK).toList();
    }

    public int reindexAllExistingProducts() {
        List<Product> products = productRepository.findAll();
        int successCount = 0;
        RestTemplate fileDownloader = new RestTemplate();

        String IMG_VIEW_BASE = "https://res.cloudinary.com/l3chl8tz/image/upload/products/";
        String IMG_VIEW_FALLBACK_BASE = "https://res.cloudinary.com/l3chl8tz/image/upload/";

        for (Product product : products) {
            try {
                if (product.getImages() == null || product.getImages().isEmpty()) {
                    continue;
                }

                Image primaryImage = product.getImages().get(0);
                String dbFileName = primaryImage.getUrl();
                if (dbFileName == null || dbFileName.isEmpty()) {
                    continue;
                }

                List<String> potentialUrls = new ArrayList<>();
                if (dbFileName.startsWith("http")) {
                    potentialUrls.add(dbFileName);
                } else {
                    potentialUrls.add(IMG_VIEW_BASE + dbFileName);
                    potentialUrls.add(IMG_VIEW_FALLBACK_BASE + dbFileName);
                }

                byte[] imageBytes = null;
                String successfullyDownloadedUrl = "";

                for (String targetUrl : potentialUrls) {
                    try {
                        imageBytes = fileDownloader.getForObject(targetUrl, byte[].class);
                        if (imageBytes != null && imageBytes.length > 0) {
                            successfullyDownloadedUrl = targetUrl;
                            break;
                        }
                    } catch (Exception e) {
                        // Thử URL tiếp theo nếu lỗi
                    }
                }

                if (imageBytes == null || imageBytes.length == 0) {
                    System.err.println("Không thể tải ảnh từ Cloudinary cho file: " + dbFileName);
                    continue;
                }

                System.out.println("Tải ảnh thành công từ Cloudinary: " + successfullyDownloadedUrl);

                MultipartFile multipartFile =
                        new MockMultipartFile(
                                "file",
                                dbFileName,
                                "image/jpeg",
                                imageBytes
                        );

                this.indexProductImage(product.getId(), product.getProductName(), multipartFile);
                successCount++;
                System.out.println(" Đã nạp thành công SP ID: " + product.getId());

            } catch (Exception e) {
                System.err.println(" Lỗi xử lý AI sản phẩm ID " + product.getId() + ": " + e.getMessage());
            }
        }
        return successCount;
    }
}