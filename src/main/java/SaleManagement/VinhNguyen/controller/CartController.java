package SaleManagement.VinhNguyen.controller;

import SaleManagement.VinhNguyen.request.CartRequest;
import SaleManagement.VinhNguyen.response.CartResponse;
import SaleManagement.VinhNguyen.service.Cart_ProductService;
import jakarta.servlet.http.Cookie; // THÊM MỚI
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private Cart_ProductService cartProductService;

    private String getAccessToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Bỏ chữ "Bearer " để lấy chuỗi token
        }
        return null;
    }

    @PostMapping("/add")
    public CartResponse add(
            HttpServletRequest request,
            @RequestBody CartRequest cartRequest){

        return cartProductService.addToCart(getAccessToken(request), cartRequest);
    }

    @PutMapping("/update")
    public CartResponse update(
            HttpServletRequest request,
            @RequestBody CartRequest cartRequest){

        return cartProductService.updateCart(getAccessToken(request), cartRequest);
    }

    @DeleteMapping("/remove/{productVariantId}")
    public CartResponse remove(
            HttpServletRequest request,
            @PathVariable Long productVariantId){

        return cartProductService.removeFromCart(getAccessToken(request), productVariantId);
    }

    @GetMapping
    public CartResponse getCart(HttpServletRequest request){
        return cartProductService.getCart(getAccessToken(request));
    }
}