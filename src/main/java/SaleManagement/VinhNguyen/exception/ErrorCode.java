package SaleManagement.VinhNguyen.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ErrorCode {
    // 404 - Không tìm thấy
    BRAND_NOT_FOUND(1001, "Brand is not found", HttpStatus.NOT_FOUND),
    BRAND_NOT_EXISTED(1003, "Brand does not existed", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND(1004, "Product is not found", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_EXISTED(1006, "Product does not existed", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(1007, "User is not found", HttpStatus.NOT_FOUND),
    CART_NOT_FOUND(1009, "Cart is not found", HttpStatus.NOT_FOUND),
    ITEM_NOT_FOUND(1010, "Item is not found in cart", HttpStatus.NOT_FOUND),
    COLOR_NOT_FOUND(1016, "Color is not found", HttpStatus.NOT_FOUND),
    SIZE_NOT_FOUND(1017, "Size is not found", HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND(1025,"Order is not found",HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND(1030,"Comment is not found",HttpStatus.NOT_FOUND),

    // 409 - Xung đột dữ liệu (Đã tồn tại)
    BRAND_EXISTED(1002, "Brand existed", HttpStatus.CONFLICT),
    PRODUCT_EXISTED(1005, "Product existed", HttpStatus.CONFLICT),
    USER_EXISTED(1011, "User existed", HttpStatus.CONFLICT),
    BRAND_IN_USE(1014, "Brand still has products", HttpStatus.CONFLICT),
    PRODUCT_CODE_EXISTED(1015,"Product code existed, Please choose new product code",HttpStatus.CONFLICT),
    COLOR_EXISTED(1018,"Color existed",HttpStatus.CONFLICT),
    SIZE_EXISTED(1019,"Size existed",HttpStatus.CONFLICT),
    // 400 - Lỗi logic người dùng
    WRONG_PASSWORD(1008, "Password is not correct", HttpStatus.BAD_REQUEST),
    RUN_OUT_OF_PRODUCT(1012, "Product is run out of", HttpStatus.BAD_REQUEST),
    CART_IS_EMPTY(1013, "Cart is empty", HttpStatus.BAD_REQUEST),
    VARIANT_DUPLICATED(1020, "Biến thể (Màu sắc và Kích thước) không được trùng lặp", HttpStatus.BAD_REQUEST),
    ACCOUNT_DISABLED(1021,"Tài khoản bị khoá" ,HttpStatus.BAD_REQUEST ),
    NO_EMAIL(1022,"Hệ thống không thể gửi Email xác thực. Vui lòng kiểm tra lại hòm thư!",HttpStatus.BAD_REQUEST),
    INVALID_OTP(1023,"Mã OTP không chính xác hoặc đã hết hạn!",HttpStatus.BAD_REQUEST),
    INVALID_GOOGLE_TOKEN(1024,"Token google không hợp lệ",HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1026,"Token is not valid",HttpStatus.BAD_REQUEST ),
    TOKEN_EXPIRED(1027,"Token is expired",HttpStatus.BAD_REQUEST ),
    TOO_MANY_LOGIN_ATTEMPTS(1028,"Cannot login because of having over 5 times" ,HttpStatus.BAD_REQUEST ),
    CANNOT_LOCK_YOURSELF(1029,"Cannot delete admin account" ,HttpStatus.BAD_REQUEST ),
    NEW_PASS_AGAIN_EQUAL_NEW_PASS(1031,"You must enter equal to new password above",HttpStatus.BAD_REQUEST);


    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public HttpStatusCode getStatusCode() { return statusCode; }
}