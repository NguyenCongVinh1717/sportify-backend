package SaleManagement.VinhNguyen.service;

import SaleManagement.VinhNguyen.entity.*;
import SaleManagement.VinhNguyen.enums.OrderStatus;
import SaleManagement.VinhNguyen.exception.AppException;
import SaleManagement.VinhNguyen.exception.ErrorCode;
import SaleManagement.VinhNguyen.mapper.OrderMapper;
import SaleManagement.VinhNguyen.repository.CartRepository;
import SaleManagement.VinhNguyen.repository.Cart_ProductRepository;
import SaleManagement.VinhNguyen.repository.OrderRepository;
import SaleManagement.VinhNguyen.repository.UserRepository;
import SaleManagement.VinhNguyen.request.OrderRequest;
import SaleManagement.VinhNguyen.response.OrderResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private Cart_ProductService cartProductService;
    @Autowired
    private CartService cartService;
    @Autowired
    private Cart_ProductRepository cartProductRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private VNPayService vnPayService;
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public OrderResponse checkout(String token, OrderRequest orderRequest, HttpServletRequest request){
        User user = cartProductService.getUser(token);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        List<Cart_Product> cartItems = cartProductRepository.findByCart(cart);
        if(cartItems.isEmpty()){
            throw new AppException(ErrorCode.CART_IS_EMPTY);
        }

        // Khởi tạo đơn hàng trống
        Order order = Order.builder()
                .receiver(orderRequest.getReceiver())
                .phone(orderRequest.getPhone())
                .address(orderRequest.getAddress())
                .user(user)
                .paymentMethod(orderRequest.getPaymentMethod()) // "COD" hoặc "VNPAY"
                .build();

        // Xử lý kiểm tra tồn kho và tính tiền
        double total = 0;
        List<OrderItem> orderItems = new ArrayList<>();
        for(Cart_Product cartProduct : cartItems){
            ProductColorSize productVariant = cartProduct.getProductColorSize();

            // Nếu biến thể hoặc sản phẩm đã bị xóa mềm/ẩn trước khi thanh toán, chặn không cho mua
            if (productVariant == null || productVariant.isDeleted() || productVariant.getProduct().isDeleted()) {
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            if(cartProduct.getQuantity() > productVariant.getStock()){
                throw new AppException(ErrorCode.RUN_OUT_OF_PRODUCT);
            }
            productVariant.setStock(productVariant.getStock() - cartProduct.getQuantity());

            // Lấy tạm ảnh đầu tiên của sản phẩm làm ảnh đại diện hóa đơn
            String firstImageUrl = "";
            if (productVariant.getProduct().getImages() != null && !productVariant.getProduct().getImages().isEmpty()) {
                firstImageUrl = productVariant.getProduct().getImages().get(0).getUrl();
            }

            // 🟢 CẬP NHẬT: Đóng băng (Snapshot) thông tin sản phẩm tại thời điểm mua
            OrderItem orderItem = OrderItem.builder()
                    .price(productVariant.getProduct().getPrice())
                    .quantity(cartProduct.getQuantity())
                    .productColorSize(productVariant)
                    .order(order)
                    .productIdSnapshot(productVariant.getProduct().getId())
                    .productNameSnapshot(productVariant.getProduct().getProductName())
                    .colorSnapshot(productVariant.getColor().getColorName())
                    .sizeSnapshot(productVariant.getSize().getSizeName())
                    .imageUrlSnapshot(firstImageUrl)
                    .build();

            orderItems.add(orderItem);
            total += productVariant.getProduct().getPrice() * cartProduct.getQuantity();
        }

        order.setOrderItems(orderItems);
        order.setTotalPrice(total);

        OrderResponse response;

        // CHIA NHÁNH LOGIC THEO ENUM VÀ PHƯƠNG THỨC THANH TOÁN
        if ("VNPAY".equalsIgnoreCase(orderRequest.getPaymentMethod())) {
            order.setStatus(OrderStatus.UNPAID); // CHƯA THANH TOÁN
            orderRepository.save(order);

            // Tạo link chuyển hướng VNPAY
            String paymentUrl = vnPayService.createPaymentUrl(order, request);

            response = OrderMapper.toResponse(order);
            response.setPaymentUrl(paymentUrl); // Gán link trả về Frontend
        } else {
            order.setStatus(OrderStatus.PENDING); // CHỜ DUYỆT (COD)
            orderRepository.save(order);

            cartProductRepository.deleteAll(cartItems); // Xóa giỏ hàng luôn vì là COD
            response = OrderMapper.toResponse(order);
        }

        return response;
    }

    @Transactional
    public void handleVNPayCallback(Map<String, String> fields) {
        String responseCode = fields.get("vnp_ResponseCode");
        String txnRef = fields.get("vnp_TxnRef");

        if (txnRef != null) {
            Long orderId = Long.parseLong(txnRef);
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

            if ("00".equals(responseCode)) {
                order.setStatus(OrderStatus.PENDING);
                orderRepository.save(order);

                Cart cart = cartRepository.findByUser(order.getUser())
                        .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
                List<Cart_Product> cartItems = cartProductRepository.findByCart(cart);
                cartProductRepository.deleteAll(cartItems);
            } else {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);

                // HOÀN STOCK: Cộng lại số lượng vào kho
                for (OrderItem item : order.getOrderItems()) {
                    ProductColorSize productVariant = item.getProductColorSize();
                    // 🟢 TRÁNH NULLPOINTER: Nếu sản phẩm/biến thể đã bị admin xóa mềm, không cố cập nhật kho nữa
                    if (productVariant != null && !productVariant.isDeleted() && !productVariant.getProduct().isDeleted()) {
                        productVariant.setStock(productVariant.getStock() + item.getQuantity());
                    }
                }
            }
        }
    }

    public List<OrderResponse> getMyOrders(String token){
        User user = cartProductService.getUser(token);
        return orderRepository.findByUser(user)
                .stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByIdDesc()
                .stream()
                .map(OrderMapper::toResponse)
                .toList();
    }
    public Page<OrderResponse> getAllOrdersForAdmin(Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAllByOrderByIdDesc(pageable);
        return orderPage.map(OrderMapper::toResponse);
    }

    public Page<OrderResponse> getOrdersByStatusForAdmin(OrderStatus status, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findByStatusOrderByIdDesc(status, pageable);
        return orderPage.map(OrderMapper::toResponse);
    }

    @Transactional
    public void updateOrderStatusByAdmin(Long orderId, String newStatusStr) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(newStatusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái đơn hàng không hợp lệ: " + newStatusStr);
        }

        // BẪY LOGIC HOÀN KHO: Nếu đổi trạng thái sang CANCELLED (Hủy đơn)
        if (newStatus == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getOrderItems()) {
                ProductColorSize productVariant = item.getProductColorSize();
                // 🟢 TRÁNH NULLPOINTER: Chỉ cộng lại kho nếu biến thể chưa bị xóa hoàn toàn
                if (productVariant != null && !productVariant.isDeleted() && !productVariant.getProduct().isDeleted()) {
                    productVariant.setStock(productVariant.getStock() + item.getQuantity());
                }
            }
        }

        // BẪY LOGIC NGƯỢC: Khôi phục đơn từ CANCELLED sang trạng thái khác
        if (order.getStatus() == OrderStatus.CANCELLED && newStatus != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getOrderItems()) {
                ProductColorSize productVariant = item.getProductColorSize();
                if (productVariant != null && !productVariant.isDeleted() && !productVariant.getProduct().isDeleted()) {
                    if (productVariant.getStock() < item.getQuantity()) {
                        throw new RuntimeException("Không thể khôi phục đơn! Sản phẩm '"
                                + item.getProductNameSnapshot() + "' đã hết hàng trong kho.");
                    }
                    productVariant.setStock(productVariant.getStock() - item.getQuantity());
                } else {
                    // Nếu sản phẩm đã bị xóa mềm, không cho phép khôi phục đơn hàng này nữa
                    throw new RuntimeException("Không thể khôi phục đơn vì sản phẩm này đã bị ngừng kinh doanh hoặc bị xóa khỏi hệ thống!");
                }
            }
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    public OrderResponse getOrderUserByOrderId(Long id){
        Order order = orderRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(String email, Long orderId){
        User user = userRepository.findByEmail(email).orElseThrow(()
                -> new AppException(ErrorCode.USER_NOT_FOUND));
        Order myOrder = orderRepository.findById(orderId).orElseThrow(()
                -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (!myOrder.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_CANCEL_ORDER);
        }
        if (myOrder.getStatus().equals(OrderStatus.DELIVERED) || myOrder.getStatus().equals(OrderStatus.SHIPPING)) {
            throw new AppException(ErrorCode.CANNOT_CANCEL_ORDER);
        }

        for (OrderItem item : myOrder.getOrderItems()) {
            if (item != null
                    && item.getProductColorSize() != null
                    && item.getProductColorSize().getProduct() != null) {

                if (!item.getProductColorSize().isDeleted() && !item.getProductColorSize().getProduct().isDeleted()) {
                    item.getProductColorSize().setStock(item.getProductColorSize().getStock() + item.getQuantity());
                }
            }
        }

        myOrder.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(myOrder);
        return OrderMapper.toResponse(myOrder);
    }
}