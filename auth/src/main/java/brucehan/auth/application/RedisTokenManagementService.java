package brucehan.auth.application;

import brucehan.auth.infrastructure.TokenManagementService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisTokenManagementService implements TokenManagementService {
    private static final String LOGOUT = "logout";
    private static final Duration ACCESS_TOKEN_TIMEOUT = Duration.ofHours(1);
    private static final Duration REFRESH_TOKEN_TIMEOUT = Duration.ofDays(7);

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean isAlreadyLogout(String token) {
        String logout = redisTemplate.opsForValue().get(token);
        return StringUtils.equals(logout, LOGOUT);
    }

    @Override
    public void banAccessToken(String token) {
        banToken(token, ACCESS_TOKEN_TIMEOUT);
    }

    @Override
    public void banRefreshToken(String token) {
        banToken(token, REFRESH_TOKEN_TIMEOUT);
    }

    private void banToken(String token, Duration timeout) {
        redisTemplate.opsForValue().set(token, LOGOUT, timeout);
    }

    @Override
    public void clear() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys == null) {
            return;
        }
        for (String key : keys) {
            if (LOGOUT.equals(redisTemplate.opsForValue().get(key))) {
                redisTemplate.delete(key);
            }
        }
    }

}
