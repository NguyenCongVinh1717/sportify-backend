package SaleManagement.VinhNguyen.controller;

import SaleManagement.VinhNguyen.request.ProductRequest;
import SaleManagement.VinhNguyen.response.CustomPageResponse;
import SaleManagement.VinhNguyen.response.ProductResponse;
import SaleManagement.VinhNguyen.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public CustomPageResponse<ProductResponse> getAllProductsPaged(
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) List<Long> colorIds,
            @RequestParam(required = false) List<Long> sizeIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        boolean hasFilter = maxPrice != null
                || brandId != null
                || (colorIds != null && !colorIds.isEmpty())
                || (sizeIds != null && !sizeIds.isEmpty());

        if (hasFilter) {
            return productService.filterProducts(maxPrice, brandId, colorIds, sizeIds, pageable);
        }

        return productService.getAllProductsPaged(pageable);
    }

    @GetMapping("/all")
    public List<ProductResponse> getAllProducts(){
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id){
        return productService.getProductById(id);
    }

    @GetMapping("/{id}/related")
    public CustomPageResponse<ProductResponse> getRelatedProductsPaged(
            @PathVariable Long id,
            Pageable pageable) {
        return productService.getRelatedProductsPaged(id, pageable);
    }

    @GetMapping("/suggestions")
    public List<String> getSearchSuggestions(
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        return productService.getSearchSuggestions(keyword);
    }

    @PostMapping
    public ProductResponse createProduct(@RequestBody ProductRequest productRequest){
        return productService.createProduct(productRequest);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable Long id, @RequestBody ProductRequest productRequest){
        return productService.updateProduct(id, productRequest);
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
    public CustomPageResponse<ProductResponse> getProductsByBrandIdPaged(
            @PathVariable Long brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return productService.getProductsByBrandIdPaged(brandId, pageable);
    }

    @GetMapping("/search")
    public CustomPageResponse<ProductResponse> getProductsByKeywords(
            @RequestParam String keywords,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productService.getProductsByKeywords(keywords, pageable);
    }
}