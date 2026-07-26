package SaleManagement.VinhNguyen.controller;

import SaleManagement.VinhNguyen.request.WishlistRequest;
import SaleManagement.VinhNguyen.response.ProductResponse;
import SaleManagement.VinhNguyen.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @PostMapping
    public String addToWishlist(
            @RequestBody @Valid WishlistRequest request,
            Principal principal) {

        String email = principal.getName();
        wishlistService.addToWishlist(email, request);

        return "Add to wishlist successfully";
    }
    @DeleteMapping("/{productId}")
    public String removeFromWishlist(
            @PathVariable Long productId,
            Principal principal) {

        String email = principal.getName();
        wishlistService.removeFromWishlist(email, productId);

        return "Delete from wishlist successfully";
    }

    @GetMapping
    public List<ProductResponse> getUserWishlist(Principal principal) {
        String email = principal.getName();
        return wishlistService.getUserWishlist(email);
    }
}