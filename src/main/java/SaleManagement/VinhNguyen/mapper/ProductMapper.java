package SaleManagement.VinhNguyen.mapper;

import SaleManagement.VinhNguyen.entity.Product;
import SaleManagement.VinhNguyen.request.ProductRequest;
import SaleManagement.VinhNguyen.response.ProductResponse;
import SaleManagement.VinhNguyen.response.ProductVariantResponse;

import java.util.stream.Collectors;

public class ProductMapper {
    public static Product toEntity(ProductRequest productRequest){
        return Product.builder()
                .productCode(productRequest.getProductCode())
                .productName(productRequest.getProductName())
                .price(productRequest.getPrice())
                .description(productRequest.getDescription())
                .build();
    }

    public static ProductResponse toResponse(Product product){
        int totalQuantity = 0;

        //caculate product's quantity is_delete=false
        if (product.getProductVariants() != null) {
            totalQuantity = product.getProductVariants()
                    .stream()
                    .filter(v -> !v.isDeleted())
                    .mapToInt(x -> x.getStock())
                    .sum();
        }

        ProductResponse.ProductResponseBuilder productResponse = ProductResponse.builder()
                .id(product.getId())
                .productCode(product.getProductCode())
                .productName(product.getProductName())
                .price(product.getPrice())
                .quantity(totalQuantity)
                .description(product.getDescription());

        if(product.getBrand() != null){
            productResponse.brandId(product.getBrand().getId());
            productResponse.brandCode(product.getBrand().getBrandCode());
            productResponse.brandName(product.getBrand().getBrandName());
        }

        if (product.getImages() != null) {
            productResponse.images(product.getImages().stream()
                    .map(img -> img.getUrl())
                    .collect(Collectors.toList()));
        }

        // ===== VARIANTS =====
        if(product.getProductVariants() != null) {
            productResponse.variants(
                    product.getProductVariants()
                            .stream()
                            .filter(v -> !v.isDeleted())
                            .map(v -> ProductVariantResponse.builder()
                                    .id(v.getId())
                                    .colorId(v.getColor().getId())
                                    .colorCode(v.getColor().getColorCode())
                                    .colorName(v.getColor().getColorName())
                                    .sizeId(v.getSize().getId())
                                    .sizeCode(v.getSize().getSizeCode())
                                    .sizeName(v.getSize().getSizeName())
                                    .stock(v.getStock())
                                    .build())
                            .collect(Collectors.toList())
            );
        }
        return productResponse.build();
    }
}