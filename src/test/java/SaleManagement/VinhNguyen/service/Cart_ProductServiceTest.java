package SaleManagement.VinhNguyen.service;

import SaleManagement.VinhNguyen.entity.*;
import SaleManagement.VinhNguyen.exception.AppException;
import SaleManagement.VinhNguyen.exception.ErrorCode;
import SaleManagement.VinhNguyen.repository.*;
import SaleManagement.VinhNguyen.request.CartRequest;
import SaleManagement.VinhNguyen.response.CartResponse;
import SaleManagement.VinhNguyen.security.JwtService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class Cart_ProductServiceTest {

    @InjectMocks
    private Cart_ProductService cartProductService;

    @Mock
    private Cart_ProductRepository cartProductRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CartService cartService;
    @Mock
    private ProductColorSizeRepository productColorSizeRepository;

    private String token;
    private User mockUser;
    private Cart mockCart;
    private Product mockParentProduct;
    private ProductColorSize mockVariant;

    @BeforeEach
    void setUp() {
        token = "mock_jwt_token";

        mockUser = new User();
        mockUser.setEmail("vinh@gmail.com");

        mockCart = new Cart();
        mockCart.setId(1L);
        mockCart.setCart_products(new ArrayList<>());

        mockParentProduct = new Product();
        mockParentProduct.setDeleted(false);

        mockVariant = new ProductColorSize();
        mockVariant.setId(10L);
        mockVariant.setStock(10);
        mockVariant.setDeleted(false);
        mockVariant.setProduct(mockParentProduct);
    }

    private void mockUserAndCart() {
        Mockito.when(jwtService.extractEmail(token)).thenReturn("vinh@gmail.com");
        Mockito.when(userRepository.findByEmail("vinh@gmail.com")).thenReturn(Optional.of(mockUser));
        Mockito.when(cartService.getOrCreateCart(mockUser)).thenReturn(mockCart);
    }

    // =========================================================================
    // 1. TEST ADD TO CART
    // =========================================================================

    @Test
    @DisplayName("TC-ADD-01: User không tồn tại -> USER_NOT_FOUND")
    void addToCart_UserNotFound_ThrowsException() {
        Mockito.when(jwtService.extractEmail(token)).thenReturn("vinh@gmail.com");
        Mockito.when(userRepository.findByEmail("vinh@gmail.com")).thenReturn(Optional.empty());

        AppException ex = Assertions.assertThrows(AppException.class,
                () -> cartProductService.addToCart(token, new CartRequest(10L, 1)));

        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("TC-ADD-03: Sản phẩm bị xóa mềm -> STOP_SELL")
    void addToCart_ProductSoftDeleted_ThrowsException() {
        mockUserAndCart();
        mockVariant.setDeleted(true); // Đánh dấu xóa mềm

        Mockito.when(productColorSizeRepository.findById(10L)).thenReturn(Optional.of(mockVariant));

        AppException ex = Assertions.assertThrows(AppException.class,
                () -> cartProductService.addToCart(token, new CartRequest(10L, 1)));

        Assertions.assertEquals(ErrorCode.STOP_SELL, ex.getErrorCode());
    }

    @Test
    @DisplayName("TC-ADD-05: SP đã có trong giỏ + Mua thêm hợp lệ -> Cộng dồn quantity")
    void addToCart_ExistingItem_Success() {
        mockUserAndCart();

        Cart_Product existingItem = Cart_Product.builder()
                .cart(mockCart)
                .productColorSize(mockVariant)
                .quantity(2)
                .build();

        Mockito.when(productColorSizeRepository.findById(10L)).thenReturn(Optional.of(mockVariant));
        Mockito.when(cartProductRepository.findByCartAndProductColorSize(mockCart, mockVariant))
                .thenReturn(Optional.of(existingItem));

        CartResponse response = cartProductService.addToCart(token, new CartRequest(10L, 3));

        Assertions.assertNotNull(response);
        Assertions.assertEquals(5, existingItem.getQuantity()); // 2 + 3 = 5
    }

    @Test
    @DisplayName("TC-ADD-07: SP chưa có trong giỏ + Mua mới hợp lệ -> Tạo mới Cart_Product")
    void addToCart_NewItem_Success() {
        mockUserAndCart();

        Mockito.when(productColorSizeRepository.findById(10L)).thenReturn(Optional.of(mockVariant));
        Mockito.when(cartProductRepository.findByCartAndProductColorSize(mockCart, mockVariant))
                .thenReturn(Optional.empty());

        CartResponse response = cartProductService.addToCart(token, new CartRequest(10L, 2));

        Assertions.assertNotNull(response);
        Mockito.verify(cartProductRepository, Mockito.times(1)).save(any(Cart_Product.class));
    }

    // =========================================================================
    // 2. TEST UPDATE CART
    // =========================================================================

    @Test
    @DisplayName("TC-UPD-02: Cập nhật số lượng vượt tồn kho -> RUN_OUT_OF_PRODUCT")
    void updateCart_ExceedStock_ThrowsException() {
        mockUserAndCart();

        Cart_Product existingItem = Cart_Product.builder()
                .cart(mockCart)
                .productColorSize(mockVariant)
                .quantity(2)
                .build();

        Mockito.when(productColorSizeRepository.findById(10L)).thenReturn(Optional.of(mockVariant));
        Mockito.when(cartProductRepository.findByCartAndProductColorSize(mockCart, mockVariant))
                .thenReturn(Optional.of(existingItem));

        // Mua 15 cái vượt quá kho (stock = 10)
        AppException ex = Assertions.assertThrows(AppException.class,
                () -> cartProductService.updateCart(token, new CartRequest(10L, 15)));

        Assertions.assertEquals(ErrorCode.RUN_OUT_OF_PRODUCT, ex.getErrorCode());
    }

    // =========================================================================
    // 3. TEST REMOVE FROM CART
    // =========================================================================

    @Test
    @DisplayName("TC-REM-01: Xóa sản phẩm khỏi giỏ -> Thành công")
    void removeFromCart_Success() {
        mockUserAndCart();

        Cart_Product item = Cart_Product.builder()
                .cart(mockCart)
                .productColorSize(mockVariant)
                .quantity(1)
                .build();
        mockCart.getCart_products().add(item);

        Mockito.when(productColorSizeRepository.findById(10L)).thenReturn(Optional.of(mockVariant));
        Mockito.when(cartProductRepository.findByCartAndProductColorSize(mockCart, mockVariant))
                .thenReturn(Optional.of(item));

        CartResponse response = cartProductService.removeFromCart(token, 10L);

        Assertions.assertNotNull(response);
        Assertions.assertTrue(mockCart.getCart_products().isEmpty());
        Mockito.verify(cartProductRepository, Mockito.times(1)).delete(item);
    }

    // =========================================================================
    // 4. TEST GET CART
    // =========================================================================

    @Test
    @DisplayName("TC-GET-01: Lấy giỏ hàng -> Tự động xóa sản phẩm bị ngưng bán (deleted)")
    void getCart_AutoCleanInvalidItems() {
        mockUserAndCart();

        // Giả sử có 1 sản phẩm bị xóa mềm nằm trong giỏ
        ProductColorSize deletedVariant = new ProductColorSize();
        deletedVariant.setDeleted(true);

        Cart_Product invalidItem = Cart_Product.builder()
                .cart(mockCart)
                .productColorSize(deletedVariant)
                .build();

        mockCart.getCart_products().add(invalidItem);

        Mockito.when(cartProductRepository.findByCart(mockCart)).thenReturn(List.of(invalidItem));

        CartResponse response = cartProductService.getCart(token);

        Assertions.assertNotNull(response);
        // Kiểm tra xem hàm deleteAll đã được gọi để dọn rác chưa
        Mockito.verify(cartProductRepository, Mockito.times(1)).deleteAll(any());
    }
}