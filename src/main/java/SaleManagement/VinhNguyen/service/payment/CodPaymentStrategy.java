package SaleManagement.VinhNguyen.service.payment;

import SaleManagement.VinhNguyen.entity.Cart;
import SaleManagement.VinhNguyen.entity.Cart_Product;
import SaleManagement.VinhNguyen.entity.Order;
import SaleManagement.VinhNguyen.enums.OrderStatus;
import SaleManagement.VinhNguyen.repository.Cart_ProductRepository;
import SaleManagement.VinhNguyen.response.OrderResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CodPaymentStrategy implements PaymentStrategy {

    @Autowired
    private Cart_ProductRepository cartProductRepository;

    @Override
    public String getPaymentMethodName() {
        return "COD";
    }

    @Override
    public OrderStatus getInitialOrderStatus() {
        return OrderStatus.PENDING;
    }

    @Override
    public void processPayment(Order order, OrderResponse response, HttpServletRequest request) {
        if (order.getUser() != null && order.getUser().getCart() != null) {
            Cart cart = order.getUser().getCart();
            List<Cart_Product> cartItems = cartProductRepository.findByCart(cart);
            if (!cartItems.isEmpty()) {
                cartProductRepository.deleteAll(cartItems);
            }
        }
    }
}