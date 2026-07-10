package SaleManagement.VinhNguyen.controller;

import SaleManagement.VinhNguyen.repository.ProductRepository;
import SaleManagement.VinhNguyen.request.ProductRequest;
import SaleManagement.VinhNguyen.response.ProductResponse;
import SaleManagement.VinhNguyen.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public Page<ProductResponse> getAllProductsPaged(
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Long brandId,        // ✨ THÊM MỚI: lọc theo thương hiệu
            @RequestParam(required = false) List<Long> colorIds, // ✨ THÊM MỚI: lọc theo màu sắc (nhiều màu)
            @RequestParam(required = false) List<Long> sizeIds,  // ✨ THÊM MỚI: lọc theo kích cỡ (nhiều size)
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ){
        // Sắp xếp tự động theo ID giảm dần
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // ✨ THÊM MỚI: Nếu có BẤT KỲ điều kiện lọc nào (giá / thương hiệu / màu / size)
        // -> gọi bộ lọc tổng hợp để áp dụng tất cả cùng lúc (khớp với nút "ÁP DỤNG BỘ LỌC" ở frontend)
        boolean hasFilter = maxPrice != null
                || brandId != null
                || (colorIds != null && !colorIds.isEmpty())
                || (sizeIds != null && !sizeIds.isEmpty());

        if (hasFilter) {
            return productService.filterProducts(maxPrice, brandId, colorIds, sizeIds, pageable);
        }

        // Không có điều kiện lọc nào -> giữ nguyên luồng lấy tất cả như cũ
        return productService.getAllProductsPaged(pageable);
    }

//    @GetMapping
//    public Page<ProductResponse> getAllProductsPaged(
//            @RequestParam(required = false) Double maxPrice,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "12") int size
//    ){
//        // Sắp xếp tự động theo ID giảm dần
//        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
//
//        // Nếu Frontend có truyền maxPrice lên, gọi hàm lọc theo giá
//        if (maxPrice != null) {
//            return productService.getProductsByPricePaged(maxPrice, pageable);
//        }
//
//        // Ngược lại nếu không truyền thì chạy luồng lấy tất cả như cũ của bạn
//        return productService.getAllProductsPaged(pageable);
//    }

    @GetMapping("/all")
    public List<ProductResponse> getAllProducts(){
        return productService.getAllProducts();
    }
    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id){
        return productService.getProductById(id);
    }

    @GetMapping("/{id}/related")
    public Page<ProductResponse> getRelatedProductsPaged(
            @PathVariable Long id,
            Pageable pageable) {
        return productService.getRelatedProductsPaged(id, pageable);
    }

    @PostMapping
    public ProductResponse createProduct(@RequestBody ProductRequest productRequest){
        return productService.createProduct(productRequest);
    }
    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable Long id,@RequestBody ProductRequest productRequest){
        return productService.updateProduct(id,productRequest);
    }
    @DeleteMapping("/{id}")
    public String deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
        return "Deleted successfully";
    }

    @DeleteMapping("/upload/{fileName}")
    public String deletePhysicalFile(@PathVariable String fileName){
        productService.deletePhysicalFile(fileName);
        return "Deleted successfully";
    }

    @GetMapping("/brand/{brandId}")
    public List<ProductResponse> getProductsByBrandId(@PathVariable Long brandId){
        return productService.getProductsByBrandId(brandId);
    }

    @GetMapping("/brandPaged/{brandId}")
    public Page<ProductResponse> getProductsByBrandIdPaged(
            @PathVariable Long brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return productService.getProductsByBrandIdPaged(brandId, pageable);
    }
    @GetMapping("/search")
    public Page<ProductResponse> getProductsByKeywords(
            @RequestParam String keywords,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productService.getProductsByKeywords(keywords, pageable);
    }

}
