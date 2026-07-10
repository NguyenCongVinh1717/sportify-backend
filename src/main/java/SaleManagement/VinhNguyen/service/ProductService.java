package SaleManagement.VinhNguyen.service;

import SaleManagement.VinhNguyen.entity.*;
import SaleManagement.VinhNguyen.exception.AppException;
import SaleManagement.VinhNguyen.exception.ErrorCode;
import SaleManagement.VinhNguyen.mapper.ProductMapper;
import SaleManagement.VinhNguyen.repository.BrandRepository;
import SaleManagement.VinhNguyen.repository.ColorRepository;
import SaleManagement.VinhNguyen.repository.ProductRepository;
import SaleManagement.VinhNguyen.repository.SizeRepository;
import SaleManagement.VinhNguyen.request.ProductRequest;
import SaleManagement.VinhNguyen.request.ProductVariantRequest;
import SaleManagement.VinhNguyen.response.ProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private ColorRepository colorRepository;
    @Autowired
    private SizeRepository sizeRepository;

    @Value("${upload.path}")
    private String uploadPath;

    public Page<ProductResponse> filterProducts(
            Double maxPrice,
            Long brandId,
            List<Long> colorIds,
            List<Long> sizeIds,
            Pageable pageable
    ) {
        Page<Product> productPage = productRepository.filterProducts(
                maxPrice, brandId, colorIds, sizeIds, pageable
        );
        return productPage.map(ProductMapper::toResponse);
    }

    public Page<ProductResponse> getAllProductsPaged(Pageable pageable) {
        Page<Product> productPage = productRepository.findAll(pageable);
        return productPage.map(ProductMapper::toResponse);
    }

    public List<ProductResponse> getAllProducts(){
        return productRepository.findAll().stream()
                .map(ProductMapper::toResponse)
                .collect(Collectors.toList());
    }

    public Page<ProductResponse> getProductsByBrandIdPaged(Long brandId, Pageable pageable) {
        Page<Product> productPage = productRepository.findByBrandId(brandId, pageable);
        return productPage.map(ProductMapper::toResponse);
    }

    public ProductResponse getProductById(Long id){
        Product product = productRepository.findById(id).orElseThrow(() ->
                new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductMapper.toResponse(product);
    }

    public List<ProductResponse> getProductsByBrandId(Long brandId) {
        return productRepository.findByBrandId(brandId).stream()
                .map(ProductMapper::toResponse)
                .collect(Collectors.toList());
    }

    public Page<ProductResponse> getProductsByKeywords(String keywords, Pageable pageable) {
        String cleanKeywords = keywords != null ? keywords.trim() : "";
        return productRepository.findByProductNameContainingIgnoreCase(cleanKeywords, pageable)
                .map(ProductMapper::toResponse);
    }

    public Page<ProductResponse> getRelatedProductsPaged(Long id, Pageable pageable) {
        Product currentProduct = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Long brandId = currentProduct.getBrand().getId();

        Page<Product> productPage = productRepository.findRelatedProductsPaged(brandId, id, pageable);
        return productPage.map(ProductMapper::toResponse);
    }
//    public Page<ProductResponse> getProductsByPricePaged(Double maxPrice, Pageable pageable) {
//        return productRepository.findByPriceLessThanEqual(maxPrice, pageable)
//                .map(ProductMapper::toResponse);
//    }


    public void deletePhysicalFile(String urlOrFileName) {
        if (urlOrFileName == null || urlOrFileName.isEmpty()) return;

        // get fileName
        String fileName = urlOrFileName.contains("/")
                ? urlOrFileName.substring(urlOrFileName.lastIndexOf("/") + 1)
                : urlOrFileName;

        // Tự động xử lý ký tự gạch chéo phân cách thư mục chuẩn theo Windows (\) hoặc Ubuntu (/)
        File file = Paths.get(uploadPath, fileName).toFile();
        if (file.exists()) {
            file.delete();
            System.out.println(">>> Đã xóa file vật lý thành công: " + file.getAbsolutePath());
        }
    }

    // --- Hàm Helper để kiểm tra trùng lặp biến thể ---
    private void validateVariants(List<ProductVariantRequest> variants) {
        if (variants == null || variants.isEmpty()) return;

        Set<String> checkSet = new HashSet<>();
        for (ProductVariantRequest v : variants) {
            String key = v.getColorId() + "-" + v.getSizeId();
            if (checkSet.contains(key)) {
                throw new AppException(ErrorCode.VARIANT_DUPLICATED);
            }
            checkSet.add(key);
        }
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest){
        if(productRepository.existsByProductCode(productRequest.getProductCode())){
            throw new AppException(ErrorCode.PRODUCT_EXISTED);
        }
        validateVariants(productRequest.getVariants());
        Brand brand = brandRepository.findById(productRequest.getBrandId())
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        Product product = ProductMapper.toEntity(productRequest);
        product.setBrand(brand);
        product.setImages(new ArrayList<>());

        if(productRequest.getImages() != null){
            productRequest.getImages().forEach(url -> {
                Image img = Image.builder()
                        .url(url)
                        .product(product)
                        .build();
                product.getImages().add(img);
            });
        }

        // VARIANTS
        product.setProductVariants(new ArrayList<>());
        if(productRequest.getVariants() != null){
            productRequest.getVariants().forEach(v -> {
                Color color = colorRepository.findById(v.getColorId())
                        .orElseThrow(() -> new AppException(ErrorCode.COLOR_NOT_FOUND));

                Size size = sizeRepository.findById(v.getSizeId())
                        .orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_FOUND));

                ProductColorSize pcs = ProductColorSize.builder()
                        .product(product)
                        .color(color)
                        .size(size)
                        .stock(v.getStock())
                        .build();

                product.getProductVariants().add(pcs);
            });
        }
        productRepository.save(product);
        return ProductMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        Product oldProduct = productRepository
                .findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        validateVariants(productRequest.getVariants());
        Brand brand = brandRepository.findById(productRequest.getBrandId())
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));

        String oldProductCode = productRequest.getProductCode();
        if(!oldProductCode.equalsIgnoreCase(oldProduct.getProductCode())){
            if(productRepository.existsByProductCode(oldProductCode)){
                throw new AppException(ErrorCode.PRODUCT_CODE_EXISTED);
            }
        }

        oldProduct.setProductName(productRequest.getProductName());
        oldProduct.setPrice(productRequest.getPrice());
        oldProduct.setBrand(brand);

        // --- Handle images ---
        List<String> newUrls = productRequest.getImages() != null ? productRequest.getImages() : new ArrayList<>();

        // Tìm những ảnh cũ CẦN XOÁ (Có trong DB nhưng không có trong Request mới)
        List<Image> toDelete = oldProduct.getImages().stream()
                .filter(img -> !newUrls.contains(img.getUrl()))
                .collect(Collectors.toList());

        // Gọi hàm helper dọn dẹp file vật lý
        for (Image img : toDelete) {
            deletePhysicalFile(img.getUrl());
        }

        // Xoá các bản ghi Image cũ khỏi Product
        oldProduct.getImages().removeAll(toDelete);

        // Thêm những ảnh mới hoàn toàn
        List<String> existingUrls = oldProduct.getImages().stream()
                .map(Image::getUrl)
                .collect(Collectors.toList());
        for (String url : newUrls) {
            if (!existingUrls.contains(url)) {
                Image newImg = Image.builder()
                        .url(url)
                        .product(oldProduct)
                        .build();
                oldProduct.getImages().add(newImg);
            }
        }

        // ================= VARIANTS =================
        oldProduct.getProductVariants().clear();
        if(productRequest.getVariants() != null){
            productRequest.getVariants().forEach(v -> {
                Color color = colorRepository.findById(v.getColorId())
                        .orElseThrow(() -> new AppException(ErrorCode.COLOR_NOT_FOUND));

                Size size = sizeRepository.findById(v.getSizeId())
                        .orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_FOUND));

                ProductColorSize pcs = ProductColorSize.builder()
                        .product(oldProduct)
                        .color(color)
                        .size(size)
                        .stock(v.getStock())
                        .build();

                oldProduct.getProductVariants().add(pcs);
            });
        }
        productRepository.save(oldProduct);
        return ProductMapper.toResponse(oldProduct);
    }

    @Transactional
    public void deleteProduct(Long id){
        Product oldProduct = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if(oldProduct.getImages() != null){
            for(Image img : oldProduct.getImages()){
                deletePhysicalFile(img.getUrl());
            }
        }
        productRepository.delete(oldProduct);
    }
}