package SaleManagement.VinhNguyen.mapper;

import SaleManagement.VinhNguyen.entity.Cart;
import SaleManagement.VinhNguyen.entity.Cart_Product;
import SaleManagement.VinhNguyen.entity.ProductColorSize;
import SaleManagement.VinhNguyen.response.CartResponse;
import SaleManagement.VinhNguyen.response.Cart_ProductResponse;

import java.util.Collections;
import java.util.stream.Collectors;

public class CartMapper {
    public static CartResponse toResponse(Cart cart){
        if (cart == null) {
            return null;
        }

        CartResponse.CartResponseBuilder cartResponseBuilder = CartResponse.builder()
                .id(cart.getId());

        if (cart.getCart_products() != null) {
            cartResponseBuilder.items(cart.getCart_products().stream()
                    // Skip soft deleted product
                    .filter(cp -> cp != null && cp.getProductColorSize() != null
                            && !cp.getProductColorSize().isDeleted()
                            && cp.getProductColorSize().getProduct() != null
                            && !cp.getProductColorSize().getProduct().isDeleted())
                    .map(CartMapper::toCart_ProductResponse)
                    .collect(Collectors.toList()));
        } else {
            cartResponseBuilder.items(Collections.emptyList());
        }

        return cartResponseBuilder.build();
    }

    private static Cart_ProductResponse toCart_ProductResponse(Cart_Product cartProduct){
        ProductColorSize pcs = cartProduct.getProductColorSize();

        // get image
        String firstImage = null;
        if (pcs.getProduct() != null && pcs.getProduct().getImages() != null && !pcs.getProduct().getImages().isEmpty()) {
            firstImage = pcs.getProduct().getImages().get(0).getUrl();
        }

        return Cart_ProductResponse.builder()
                .id(cartProduct.getId())
                .productColorSizeId(pcs.getId())
                .productCode(pcs.getProduct() != null ? pcs.getProduct().getProductCode() : "N/A")
                .productName(pcs.getProduct() != null ? pcs.getProduct().getProductName() : "Sản phẩm không xác định")
                .colorName(pcs.getColor() != null ? pcs.getColor().getColorName() : "N/A")
                .sizeName(pcs.getSize() != null ? pcs.getSize().getSizeName() : "N/A")
                .price(pcs.getProduct() != null ? pcs.getProduct().getPrice() : 0.0)
                .quantity(cartProduct.getQuantity())
                .image(firstImage)
                .build();
    }
}