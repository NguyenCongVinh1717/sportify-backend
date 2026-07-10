package SaleManagement.VinhNguyen.service;

import SaleManagement.VinhNguyen.entity.*;
import SaleManagement.VinhNguyen.enums.OrderStatus;
import SaleManagement.VinhNguyen.exception.AppException;
import SaleManagement.VinhNguyen.exception.ErrorCode;
import SaleManagement.VinhNguyen.mapper.OrderMapper;
import SaleManagement.VinhNguyen.repository.CartRepository;
import SaleManagement.VinhNguyen.repository.Cart_ProductRepository;
import SaleManagement.VinhNguyen.repository.OrderRepository;
import SaleManagement.VinhNguyen.request.OrderRequest;
import SaleManagement.VinhNguyen.response.OrderResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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
                .paymentMethod(orderRequest.getPaymentMethod()) // Nhận từ Request: "COD" hoặc "VNPAY"
                .build();

        // Xử lý kiểm tra tồn kho và tính tiền
        double total = 0;
        List<OrderItem> orderItems = new ArrayList<>();
        for(Cart_Product cartProduct : cartItems){
            ProductColorSize product = cartProduct.getProductColorSize();
            if(cartProduct.getQuantity() > product.getStock()){
                throw new AppException(ErrorCode.RUN_OUT_OF_PRODUCT);
            }
            product.setStock(product.getStock() - cartProduct.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .price(product.getProduct().getPrice())
                    .quantity(cartProduct.getQuantity())
                    .productColorSize(product)
                    .order(order)
                    .build();
            orderItems.add(orderItem);
            total += product.getProduct().getPrice() * cartProduct.getQuantity();
        }

        order.setOrderItems(orderItems);
        order.setTotalPrice(total);

        OrderResponse response;

        // CHIA NHÁNH LOGIC THEO ENUM VÀ PHƯƠNG THỨC THANH TOÁN
        if ("VNPAY".equalsIgnoreCase(orderRequest.getPaymentMethod())) {
            order.setStatus(OrderStatus.UNPAID); // Đặt trạng thái: CHƯA THANH TOÁN
            orderRepository.save(order);

            // Tạo link chuyển hướng VNPAY
            String paymentUrl = vnPayService.createPaymentUrl(order, request);

            response = OrderMapper.toResponse(order);
            response.setPaymentUrl(paymentUrl); // Gán link trả về Frontend

            // Lưu ý: Đơn online chưa trả tiền thì chưa xóa giỏ hàng ở đây.
            // Ta sẽ xóa giỏ hàng ở hàm IPN/Callback khi VNPAY báo trả tiền thành công.
        } else {
            order.setStatus(OrderStatus.PENDING); // Đặt trạng thái: CHỜ DUYỆT (COD)
            orderRepository.save(order);

            cartProductRepository.deleteAll(cartItems); // Xóa giỏ hàng luôn vì là COD
            response = OrderMapper.toResponse(order);
        }

        return response;
    }

    @Transactional
    public void handleVNPayCallback(Map<String, String> fields) {
        String responseCode = fields.get("vnp_ResponseCode");
        String txnRef = fields.get("vnp_TxnRef"); // Đây chính là Order ID ta truyền đi lúc tạo link

        if (txnRef != null) {
            Long orderId = Long.parseLong(txnRef);
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

            // vnp_ResponseCode == "00" nghĩa là người dùng đã chuyển khoản THÀNH CÔNG
            if ("00".equals(responseCode)) {
                order.setStatus(OrderStatus.PENDING); // Đổi từ UNPAID sang PENDING (Chờ duyệt đi đơn)
                orderRepository.save(order);

                // Xóa giỏ hàng của người dùng vì họ đã trả tiền xong
                Cart cart = cartRepository.findByUser(order.getUser())
                        .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
                List<Cart_Product> cartItems = cartProductRepository.findByCart(cart);
                cartProductRepository.deleteAll(cartItems);
            } else {
                // Người dùng hủy thanh toán hoặc giao dịch lỗi
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);

                // LƯU Ý HOÀN STOCK: Vì lúc checkout ta đã trừ stock của sản phẩm,
                // nếu họ hủy không trả tiền, bạn nên viết thêm logic cộng lại số lượng vào kho ở đây nhé!
                for (OrderItem item : order.getOrderItems()) {
                    ProductColorSize product = item.getProductColorSize();
                    product.setStock(product.getStock() + item.getQuantity());
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

    public List<OrderResponse> getAllOrdersForAdmin() {
        // Bạn có thể viết thêm một hàm findByOrderByIdDesc() trong OrderRepository nếu muốn,
        // ở đây dùng findAll() rồi chuyển sang DTO Response.
        return orderRepository.findAll().stream()
                .sorted((o1, o2) -> o2.getId().compareTo(o1.getId())) // Sắp xếp đơn mới lên trước
                .map(OrderMapper::toResponse)
                .toList();
    }

    public List<OrderResponse> getOrdersByStatusForAdmin(OrderStatus status) {
        // Gọi trực tiếp Repository tìm kiếm bằng Enum luôn, cực kỳ an toàn
        List<Order> orders = orderRepository.findByStatusOrderByIdDesc(status);

        // Chuyển đổi sang List DTO Response
        return orders.stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    public void updateOrderStatusByAdmin(Long orderId, String newStatusStr) {
        // Tìm đơn hàng cần cập nhật
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // Chuyển đổi chuỗi String nhận từ Frontend (ví dụ: "SHIPPING") sang Enum OrderStatus chuẩn của hệ thống
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(newStatusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái đơn hàng không hợp lệ: " + newStatusStr);
        }

        // Kiểm tra nếu trạng thái cũ đã là COMPLETED hoặc CANCELLED thì hạn chế cho sửa đổi bừa bãi
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            // Tùy nhu cầu, bạn có thể chặn không cho Admin sửa khi đơn đã đóng:
            // throw new RuntimeException("Đơn hàng đã hoàn thành hoặc đã hủy, không thể thay đổi trạng thái!");
        }

        // BẪY LOGIC HOÀN KHO: Nếu đổi trạng thái sang CANCELLED (Hủy đơn)
        // và trước đó đơn hàng CHƯA bị hủy, tiến hành cộng lại số lượng vào kho.
        if (newStatus == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getOrderItems()) {
                ProductColorSize product = item.getProductColorSize();
                if (product != null) {
                    product.setStock(product.getStock() + item.getQuantity());
                    // Hibernate sẽ tự động ép cấu trúc UPDATE xuống DB nhờ có @Transactional
                }
            }
        }

        // BẪY LOGIC NGƯỢC: Nếu Admin lỡ tay bấm hủy, giờ khôi phục lại sang trạng thái khác
        // thì phải trừ bớt kho đi (Nếu kho còn đủ)
        if (order.getStatus() == OrderStatus.CANCELLED && newStatus != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getOrderItems()) {
                ProductColorSize product = item.getProductColorSize();
                if (product != null) {
                    if (product.getStock() < item.getQuantity()) {
                        throw new RuntimeException("Không thể khôi phục đơn! Sản phẩm '"
                                + product.getProduct().getProductName() + "' đã hết hàng trong kho.");
                    }
                    product.setStock(product.getStock() - item.getQuantity());
                }
            }
        }

        // Cập nhật trạng thái mới và lưu vào Cơ sở dữ liệu
        order.setStatus(newStatus);
        orderRepository.save(order);
    }
    public OrderResponse getOrderUserByOrderId(Long id){
        Order order=orderRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return OrderMapper.toResponse(order);
    }
}
