package brucehan.member.domain.entity;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "refresh_token", timeToLive = 60 * 60 * 24 * 7)
@RequiredArgsConstructor
@Getter
public class RefreshTokenEntity {

    @Id
    private final String id;
    private final String refreshToken;

    public boolean validateRefreshToken(String refreshToken) {
        return this.refreshToken.equals(refreshToken);
    }
}
