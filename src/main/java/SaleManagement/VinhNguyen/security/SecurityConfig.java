package SaleManagement.VinhNguyen.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse; // Nhớ import thư viện này

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. PUBLIC API - Cho phép truy cập tự do không cần Token
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/order/vnpay-callback").permitAll()
                        .requestMatchers("/images/**", "/upload/**", "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2. SẢN PHẨM / THƯƠNG HIỆU / KÍCH THƯỚC / MÀU SẮC
                        .requestMatchers(HttpMethod.GET, "/products/**", "/brands/**", "/colors/**", "/sizes/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/products/**", "/brands/**", "/colors/**", "/sizes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/products/**", "/brands/**", "/colors/**", "/sizes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/products/**", "/brands/**", "/colors/**", "/sizes/**").hasRole("ADMIN")

                        // 3. QUẢN LÝ USER / ADMIN
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // ==================== 4. CẤU HÌNH AI CHUẨN ====================
                        // Cho phép tất cả mọi người (Khách + User + Admin) sử dụng tính năng tư vấn AI
                        .requestMatchers("/ai/consult").permitAll()
                        // Chỉ ADMIN mới có quyền trigger reindex dữ liệu AI
                        .requestMatchers("/ai/reindex").hasRole("ADMIN")
                        // Các endpoint AI phát sinh khác (nếu có) cũng chặn mặc định chỉ dành cho Admin
                        .requestMatchers("/ai/**").hasRole("ADMIN")
                        // ============================================================

                        // 5. GIỎ HÀNG & ĐƠN HÀNG
                        .requestMatchers("/cart/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/order/**").hasAnyRole("USER", "ADMIN")

                        // 6. BÌNH LUẬN (COMMENTS)
                        .requestMatchers(HttpMethod.GET, "/comments/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/comments/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/comments/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/comments/**").hasAnyRole("USER", "ADMIN")

                        // Các request còn lại bắt buộc phải xác thực
                        .anyRequest().authenticated()
                )

                // Xử lý Lỗi 401 & 403
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"code\": 1026, \"message\": \"Phiên làm việc không hợp lệ hoặc thiếu Token.\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"code\": 1027, \"message\": \"Bạn không có quyền thực hiện thao tác này.\"}");
                        })
                );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
////        configuration.setAllowedOrigins(Arrays.asList(
////                "https://sportify.com", "http://localhost", "http://127.0.0.1",
////                "http://127.0.0.1:5500", "http://localhost:5500", "https://127.0.0.1:5500", "https://localhost:5500",
////                "http://127.0.0.1:5501", "https://127.0.0.1:5501",
////                "http://127.0.0.1:5555", "https://127.0.0.1:5555"
////        ));
//        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        configuration.setAllowedHeaders(Arrays.asList("*"));
//        configuration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // 1. Thay vì dùng hàm checkOrigin phức tạp dễ lỗi compile, ta dùng Custom Origin Processor:
    configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:[*]",
            "http://127.0.0.1:[*]",
            "https://*.trycloudflare.com",
            "https://sportify.com"
    ));

    // 2. Cấu hình đầy đủ các phương thức và quyền hạn
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setExposedHeaders(Arrays.asList("Authorization", "Link", "X-Total-Count"));
    configuration.setAllowCredentials(true); // Bắt buộc giữ nguyên để xóa ảnh vật lý

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
}