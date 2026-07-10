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
                        .map(items-> OrderItemResponse.builder()
                                .productName(items.getProductColorSize().getProduct().getProductName())
                                .colorName(items.getProductColorSize().getColor().getColorName())
                                .sizeName(items.getProductColorSize().getSize().getSizeName())
                                .price(items.getPrice())
                                .quantity(items.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}