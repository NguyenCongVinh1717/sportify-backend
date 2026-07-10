package SaleManagement.VinhNguyen.service;

import SaleManagement.VinhNguyen.entity.*;
import SaleManagement.VinhNguyen.exception.AppException;
import SaleManagement.VinhNguyen.exception.ErrorCode;
import SaleManagement.VinhNguyen.mapper.CartMapper;
import SaleManagement.VinhNguyen.mapper.ProductMapper;
import SaleManagement.VinhNguyen.repository.*;
import SaleManagement.VinhNguyen.request.CartRequest;
import SaleManagement.VinhNguyen.response.CartResponse;
import SaleManagement.VinhNguyen.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class Cart_ProductService {
    @Autowired
    private Cart_ProductRepository cartProductRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartService cartService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductColorSizeRepository productColorSizeRepository;

    public User getUser(String token){

        String email = jwtService.extractEmail(token);

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new AppException(ErrorCode.USER_NOT_FOUND));
    }
    @Transactional
    public CartResponse addToCart(String token,CartRequest cartRequest){
        User user=getUser(token);
        Cart cart=cartService.getOrCreateCart(user);

        ProductColorSize product=productColorSizeRepository.findById(cartRequest.getProductColorSizeId()).orElseThrow(
                () -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        Cart_Product cart_product=cartProductRepository.findByCartAndProductColorSize(cart,product).orElse(null);
        if(cart_product!=null){
            if(cart_product.getQuantity()+cartRequest.getQuantity()> product.getStock()){
                throw new AppException(ErrorCode.RUN_OUT_OF_PRODUCT);
            }
            cart_product.setQuantity(cart_product.getQuantity()+cartRequest.getQuantity());
        }
        else{
            if(cartRequest.getQuantity()> product.getStock()){
                throw new AppException(ErrorCode.RUN_OUT_OF_PRODUCT);
            }
            cart_product=Cart_Product.builder()
            .cart(cart)
            .productColorSize(product)
            .quantity(cartRequest.getQuantity()).build();
            cartProductRepository.save(cart_product);
        }
        return CartMapper.toResponse(cart);
    }

    @Transactional
    public CartResponse updateCart(String token, CartRequest cartRequest){

        User user = getUser(token);
        Cart cart = cartService.getOrCreateCart(user);

        ProductColorSize product = productColorSizeRepository.findById(cartRequest.getProductColorSizeId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Cart_Product cartProduct = cartProductRepository
                .findByCartAndProductColorSize(cart, product)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

        if(cartRequest.getQuantity()> product.getStock()){
            throw new AppException(ErrorCode.RUN_OUT_OF_PRODUCT);
        }

        cartProduct.setQuantity(cartRequest.getQuantity());
        return CartMapper.toResponse(cart);
    }

    public CartResponse removeFromCart(String token, Long productVariantId){

        User user = getUser(token);
        Cart cart = cartService.getOrCreateCart(user);

        ProductColorSize product = productColorSizeRepository.findById(productVariantId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Cart_Product item = cartProductRepository.findByCartAndProductColorSize(cart, product)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

        cartProductRepository.delete(item);
        return CartMapper.toResponse(cart);
    }
    public CartResponse getCart(String token){
        User user = getUser(token);
        Cart cart = cartService.getOrCreateCart(user);
        return CartMapper.toResponse(cart);
    }
}
