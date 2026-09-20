package brucehan.order.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisLockService {
    private final StringRedisTemplate stringRedisTemplate;

    public boolean tryLock(String key, String value) {
        return stringRedisTemplate.opsForValue().setIfAbsent(key, value);

    }

    public boolean releaseLock(String key) {
        return stringRedisTemplate.delete(key);
    }
}
