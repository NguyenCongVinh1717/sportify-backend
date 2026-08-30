package SaleManagement.VinhNguyen.mapper;

import SaleManagement.VinhNguyen.entity.Order;
import SaleManagement.VinhNguyen.response.OrderItemResponse;
import SaleManagement.VinhNguyen.response.OrderResponse;
import java.util.stream.Collectors;

public class OrderMapper {
    public static OrderResponse toResponse(Order order){
        return OrderResponse.builder()
                .id(order.getId())
                .totalPrice(order.getTotalPrice())
                .receiver(order.getReceiver())
                .address(order.getAddress())
                .phone(order.getPhone())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .paymentMethod(order.getPaymentMethod())
                .items(order.getOrderItems().stream()
                        .map(item -> OrderItemResponse.builder()
                                .productId(item.getProductIdSnapshot())
                                .productName(item.getProductNameSnapshot())
                                .colorName(item.getColorSnapshot())
                                .sizeName(item.getSizeSnapshot())
                                .price(item.getPrice())
                                .quantity(item.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}