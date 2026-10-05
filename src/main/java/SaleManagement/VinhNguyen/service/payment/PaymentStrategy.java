package SaleManagement.VinhNguyen.service.payment;

import SaleManagement.VinhNguyen.entity.Order;
import SaleManagement.VinhNguyen.enums.OrderStatus;
import SaleManagement.VinhNguyen.response.OrderResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentStrategy {
    String getPaymentMethodName();
    OrderStatus getInitialOrderStatus();
    void processPayment(Order order, OrderResponse response, HttpServletRequest request);
}