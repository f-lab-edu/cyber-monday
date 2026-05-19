package brucehan.auth.application;

import brucehan.auth.config.exception.ApplicationException;
import brucehan.auth.domain.entity.RefreshTokenEntity;
import brucehan.auth.domain.repository.RefreshTokenRedisRepository;
import brucehan.auth.infrastructure.kakao_client.dto.JwtTokenDto;
import brucehan.auth.presentation.provider.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static brucehan.auth.config.exception.ApplicationExceptionType.JWT_REFRESH_INVALID;
import static brucehan.auth.config.exception.ApplicationExceptionType.JWT_REFRESH_NOT_FOUND_IN_REDIS;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    /**
     * refresh token을 이용하여 access token과 refresh token을 재발급한다.
     * 재발급한 refresh token은 Redis에 저장한다.
     * @param refreshToken
     * @return
     */
    public JwtTokenDto reissueTokens(String refreshToken) {
        // refresh token 검증 & memberId 추출
        Long memberId = jwtTokenProvider.getMemberIdFromToken(refreshToken);

        // Redis에서 refresh token 조회
        RefreshTokenEntity refreshTokenEntity = refreshTokenRedisRepository
                .findById(memberId.toString())
                .orElseThrow(() -> new ApplicationException(JWT_REFRESH_NOT_FOUND_IN_REDIS));

        if (!refreshTokenEntity.validateRefreshToken(refreshToken)) {
            throw new ApplicationException(JWT_REFRESH_INVALID);
        }

        // 토큰 재발급
        JwtTokenDto tokens = jwtTokenProvider.generateTokens(memberId);

        // refresh token 저장
        saveRefreshToken(memberId, tokens.refreshToken());
        return tokens;
    }

    public void saveRefreshToken(Long memberId, String refreshToken) {
        refreshTokenRedisRepository.save(new RefreshTokenEntity(memberId.toString(), refreshToken));
    }
}
