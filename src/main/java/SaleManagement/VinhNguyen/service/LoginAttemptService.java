package SaleManagement.VinhNguyen.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private final int MAX_ATTEMPT = 5; // Số lần thử tối đa
    private final int LOCK_TIME_MINUTES = 15; // Thời gian khóa tạm thời

    // Tạo bộ nhớ cache tự động hết hạn sau 15 phút kể từ lượt ghi cuối cùng
    private final Cache<String, Integer> attemptsCache = Caffeine.newBuilder()
            .expireAfterWrite(LOCK_TIME_MINUTES, TimeUnit.MINUTES)
            .build();

    // Gọi khi đăng nhập thành công để reset lại bộ đếm
    public void loginSucceeded(String key) {
        attemptsCache.invalidate(key);
    }

    // Gọi khi đăng nhập thất bại để tăng bộ đếm lên 1
    public void loginFailed(String key) {
        int attempts = 0;
        Integer cachedAttempts = attemptsCache.getIfPresent(key);
        if (cachedAttempts != null) {
            attempts = cachedAttempts;
        }
        attempts++;
        attemptsCache.put(key, attempts);
    }

    // Kiểm tra xem Email này có đang bị khóa hay không
    public boolean isBlocked(String key) {
        Integer cachedAttempts = attemptsCache.getIfPresent(key);
        if (cachedAttempts != null) {
            return cachedAttempts >= MAX_ATTEMPT;
        }
        return false;
    }
}