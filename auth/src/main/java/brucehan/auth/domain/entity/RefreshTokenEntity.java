package brucehan.auth.domain.entity;

import brucehan.auth.config.exception.ApplicationException;
import brucehan.auth.config.exception.ApplicationExceptionType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import static brucehan.auth.config.exception.ApplicationExceptionType.*;

@RedisHash(value = "refresh_token", timeToLive = 1209600)
@RequiredArgsConstructor
@Getter
public class RefreshTokenEntity {
    @Id
    private final String memberId;
    private final String refreshToken;

    public static RefreshTokenEntity create(String memberId, String refreshToken) {
        return new RefreshTokenEntity(memberId, refreshToken);
    }

    public void validateRefreshToken(String token) {
        if (!refreshToken.equals(token)) {
            throw new ApplicationException(JWT_REFRESH_INVALID);
        }
    }
}
