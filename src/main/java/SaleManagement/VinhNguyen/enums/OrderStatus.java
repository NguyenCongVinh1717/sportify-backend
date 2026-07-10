package SaleManagement.VinhNguyen.enums;

public enum OrderStatus {
    UNPAID,    // Đơn hàng online vừa tạo, chưa thanh toán thành công
    PENDING,   // Đơn hàng COD (hoặc Online đã trả tiền), đang chờ admin duyệt/giao hàng
    SHIPPING,  // Đang giao hàng
    DELIVERED, // Đã giao thành công
    CANCELLED  // Đơn bị hủy
}