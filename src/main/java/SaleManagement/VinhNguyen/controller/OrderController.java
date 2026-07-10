package SaleManagement.VinhNguyen.controller;

import SaleManagement.VinhNguyen.configuration.VNPayConfig;
import SaleManagement.VinhNguyen.request.OrderRequest;
import SaleManagement.VinhNguyen.response.OrderResponse;
import SaleManagement.VinhNguyen.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private VNPayConfig vnPayConfig;

    private String getAccessToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Bỏ chữ "Bearer " để lấy chuỗi token
        }
        return null;
    }

    @PostMapping("/checkout")
    public OrderResponse checkout(
            HttpServletRequest request,
            @RequestBody OrderRequest orderRequest){
        return orderService.checkout(getAccessToken(request), orderRequest, request);
    }

    @GetMapping("/vnpay-callback")
    public Map<String, String> vnpayCallback(
            @RequestParam Map<String, String> requestParams) {

        Map<String, String> response = new HashMap<>();

        String vnp_SecureHash = requestParams.get("vnp_SecureHash");

        Map<String, String> fields = new HashMap<>(requestParams);

        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        if (vnPayConfig.verifySignature(fields, vnp_SecureHash)) {

            if ("00".equals(requestParams.get("vnp_ResponseCode"))) {

                orderService.handleVNPayCallback(requestParams);

                response.put("status", "SUCCESS");
                response.put("message", "Thanh toán thành công");
            } else {

                response.put("status", "FAILED");
                response.put("message", "Thanh toán thất bại");
            }

        } else {

            response.put("status", "INVALID_SIGNATURE");
            response.put("message", "Sai chữ ký");
        }

        return response;
    }

    @GetMapping("/myOrders")
    public List<OrderResponse> getMyOrders(HttpServletRequest request){
        return orderService.getMyOrders(getAccessToken(request));
    }
}