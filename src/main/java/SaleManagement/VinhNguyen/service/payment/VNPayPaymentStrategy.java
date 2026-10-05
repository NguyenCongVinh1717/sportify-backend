package SaleManagement.VinhNguyen.service.payment;

import SaleManagement.VinhNguyen.entity.Order;
import SaleManagement.VinhNguyen.enums.OrderStatus;
import SaleManagement.VinhNguyen.response.OrderResponse;
import SaleManagement.VinhNguyen.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VNPayPaymentStrategy implements PaymentStrategy {

    @Autowired
    private VNPayService vnPayService;

    @Override
    public String getPaymentMethodName() {
        return "VNPAY";
    }

    @Override
    public OrderStatus getInitialOrderStatus() {
        return OrderStatus.UNPAID;
    }

    @Override
    public void processPayment(Order order, OrderResponse response, HttpServletRequest request) {
        // VNPay thì tạo link chuyển hướng và gán vào response
        String paymentUrl = vnPayService.createPaymentUrl(order, request);
        response.setPaymentUrl(paymentUrl);
    }
}