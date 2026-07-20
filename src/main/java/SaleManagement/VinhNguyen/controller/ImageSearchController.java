package SaleManagement.VinhNguyen.controller;

import SaleManagement.VinhNguyen.entity.Product;
import SaleManagement.VinhNguyen.response.ProductResponse;
import SaleManagement.VinhNguyen.service.ImageSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/image")
@RequiredArgsConstructor
public class ImageSearchController {

    private final ImageSearchService imageSearchService;

    @PostMapping("/index")
    public ResponseEntity<String> indexImage(
            @RequestParam("productId") Long productId,
            @RequestParam("productName") String productName,
            @RequestParam("file") MultipartFile file) throws IOException {
        imageSearchService.indexProductImage(productId, productName, file);
        return ResponseEntity.ok("Đã nạp vector hình ảnh sản phẩm thành công");
    }

    @PostMapping("/search")
    public List<ProductResponse> searchByImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "topK", defaultValue = "6") int topK
    ) throws IOException {
        return imageSearchService.searchByImage(file, topK);
    }

    @PostMapping("/reindex-all")
    public ResponseEntity<String> reindexAllImages() {
        int successCount = imageSearchService.reindexAllExistingProducts();
        return ResponseEntity.ok("Đã nạp thành công hình ảnh cho " + successCount + " sản phẩm vào CSDL Vector.");
    }
}