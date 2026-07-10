package SaleManagement.VinhNguyen.mapper;

import SaleManagement.VinhNguyen.entity.Cart;
import SaleManagement.VinhNguyen.entity.Cart_Product;
import SaleManagement.VinhNguyen.response.CartResponse;
import SaleManagement.VinhNguyen.response.Cart_ProductResponse;

import java.util.Collections;
import java.util.stream.Collectors;

public class CartMapper {
    public static CartResponse toResponse(Cart cart){
        CartResponse.CartResponseBuilder cartResponseBuilder=CartResponse.builder()
                .id(cart.getId());
        if(cart.getCart_products()!=null){
            cartResponseBuilder.items(cart.getCart_products().stream()
                    .map(CartMapper::toCart_ProductResponse)
                    .collect(Collectors.toList()));
        }
        return cartResponseBuilder.build();
    }

    private static Cart_ProductResponse toCart_ProductResponse(Cart_Product cartProduct){
        // Get list images of the product
        var productImages = cartProduct.getProductColorSize().getProduct().getImages();

        // Get the first image
        String firstImage = (productImages != null && !productImages.isEmpty())
                ? productImages.get(0).getUrl()
                : null;
            return Cart_ProductResponse.builder()
                    .id(cartProduct.getId())
                    .productColorSizeId(cartProduct.getProductColorSize().getId())
                    .productCode(cartProduct.getProductColorSize().getProduct().getProductCode())
                    .productName(cartProduct.getProductColorSize().getProduct().getProductName())
                    .colorName(cartProduct.getProductColorSize().getColor().getColorName())
                    .sizeName(cartProduct.getProductColorSize().getSize().getSizeName())
                    .price(cartProduct.getProductColorSize().getProduct().getPrice())
                    .quantity(cartProduct.getQuantity())
                    .image(firstImage)
                    .build();
    }
}
