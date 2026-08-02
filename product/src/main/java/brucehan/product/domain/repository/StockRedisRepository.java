package brucehan.product.domain.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StockRedisRepository {
    private static final String STOCK_KEY = "stock:";
    private final RedisTemplate<String, String> redisTemplate;

    private String getKey(final Long productId) {
        return STOCK_KEY + productId;
    }

    private boolean isExisted(final Long productId) {
        return redisTemplate.hasKey(getKey(productId));
    }
}
