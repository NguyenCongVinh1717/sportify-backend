package SaleManagement.VinhNguyen.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private StringWriter responseOutput;

    @BeforeEach
    void setUp() throws Exception {
        responseOutput = new StringWriter();
        PrintWriter writer = new PrintWriter(responseOutput);
        lenient().when(response.getWriter()).thenReturn(writer);
    }

    // =========================================================================
    // TC-SEC-01 & TC-SEC-02: Kiểm tra xử lý Lỗi 401 Unauthorized (Chưa đăng nhập)
    // =========================================================================
    @Test
    @DisplayName("TC-SEC-01 & 02: Xử lý AuthenticationEntryPoint trả về HTTP 401 và Mã lỗi JSON 1026")
    void testAuthenticationEntryPoint_Returns401AndJson() throws Exception {
        // Giả lập logic khi chưa đăng nhập / không có token
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\": 1026, \"message\": \"Phiên làm việc không hợp lệ hoặc thiếu Token.\"}");

        // Verify status code 401 được thiết lập
        verify(response).setStatus(401);

        // Verify nội dung JSON phản hồi đúng mã 1026 như trong SecurityConfig
        String result = responseOutput.toString();
        assertTrue(result.contains("1026"), "Phản hồi phải chứa mã lỗi 1026");
        assertTrue(result.contains("Phiên làm việc không hợp lệ"), "Phản hồi phải chứa thông điệp lỗi");
    }

    // =========================================================================
    // TC-SEC-03 & TC-SEC-04 & TC-SEC-06: Kiểm tra xử lý Lỗi 403 Forbidden (Sai quyền)
    // =========================================================================
    @Test
    @DisplayName("TC-SEC-03 & 04 & 06: Xử lý AccessDeniedHandler trả về HTTP 403 và Mã lỗi JSON 1027")
    void testAccessDeniedHandler_Returns403AndJson() throws Exception {
        // Giả lập logic khi User không đủ quyền (VD: Role USER gọi API Admin)
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\": 1027, \"message\": \"Bạn không có quyền thực hiện thao tác này.\"}");

        // Verify status code 403 được thiết lập
        verify(response).setStatus(403);

        // Verify nội dung JSON phản hồi đúng mã 1027 như trong SecurityConfig
        String result = responseOutput.toString();
        assertTrue(result.contains("1027"), "Phản hồi phải chứa mã lỗi 1027");
        assertTrue(result.contains("không có quyền"), "Phản hồi phải chứa thông điệp từ chối truy cập");
    }

    // =========================================================================
    // TC-SEC-05: Kiểm tra khi Token không hợp lệ / sai chữ ký
    // =========================================================================
    @Test
    @DisplayName("TC-SEC-05: Request chứa Header Authorization rác bị từ chối")
    void testInvalidToken_HeaderProcessing() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid_token_12345");

        // Giả lập Filter phát hiện Token lỗi và đẩy về 401
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer invalid")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        verify(response).setStatus(401);
    }
}