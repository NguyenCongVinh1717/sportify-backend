package SaleManagement.VinhNguyen.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;

@Configuration
public class RedisConfig {

    // Khai báo Bean ObjectMapper cho AuthService và toàn bộ dự án dùng chung
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            @Value("${spring.cache.redis.time-to-live:600000}") long cacheTtlMillis) {
        // Dùng RedisSerializer.json() để Spring tự chọn Serializer phù hợp mà không lo lệch version Jackson
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(cacheTtlMillis))
                .disableCachingNullValues()
                .prefixCacheNameWith("v3::")
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new JdkSerializationRedisSerializer()
                        )
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}