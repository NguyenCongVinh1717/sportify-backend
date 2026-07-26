package SaleManagement.VinhNguyen.service;

import SaleManagement.VinhNguyen.entity.Product;
import SaleManagement.VinhNguyen.entity.User;
import SaleManagement.VinhNguyen.entity.Wishlist;
import SaleManagement.VinhNguyen.exception.AppException;
import SaleManagement.VinhNguyen.exception.ErrorCode;
import SaleManagement.VinhNguyen.mapper.ProductMapper;
import SaleManagement.VinhNguyen.repository.ProductRepository;
import SaleManagement.VinhNguyen.repository.UserRepository;
import SaleManagement.VinhNguyen.repository.WishlistRepository;
import SaleManagement.VinhNguyen.request.WishlistRequest;
import SaleManagement.VinhNguyen.response.ProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public void addToWishlist(String email, WishlistRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!wishlistRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
                Wishlist wishlist=Wishlist.builder()
                                .product(product)
                                .user(user)
                                .build();
            wishlistRepository.save(wishlist);
        }
    }

    @Transactional
    public void removeFromWishlist(String email, Long productId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!productRepository.existsById(productId)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        wishlistRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }

    public List<ProductResponse> getUserWishlist(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return wishlistRepository.findByUserId(user.getId()).stream()
                .map(w -> ProductMapper.toResponse(w.getProduct()))
                .collect(Collectors.toList());
    }

}