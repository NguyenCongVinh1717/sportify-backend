package SaleManagement.VinhNguyen.controller;

import SaleManagement.VinhNguyen.enums.OrderStatus;
import SaleManagement.VinhNguyen.response.OrderResponse;
import SaleManagement.VinhNguyen.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;


    @GetMapping
    public Page<OrderResponse> getAllOrders(@PageableDefault(size = 12) Pageable pageable) {
        return orderService.getAllOrdersForAdmin(pageable);
    }
    @GetMapping("/all")
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }
    @PutMapping("/{id}/status")
    public Map<String, String> updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status) {

        orderService.updateOrderStatusByAdmin(id, status);

        return Collections.singletonMap("message", "Success");
    }

    @GetMapping("/status/{status}")
    public Page<OrderResponse> getOrdersByStatus(
            @PathVariable("status") OrderStatus status,
            @PageableDefault(size = 12) Pageable pageable) {
        return orderService.getOrdersByStatusForAdmin(status, pageable);
    }

    @GetMapping("/orderById")
    public OrderResponse getOrderUserByOrderId(@RequestParam Long id){
        return orderService.getOrderUserByOrderId(id);
    }
}