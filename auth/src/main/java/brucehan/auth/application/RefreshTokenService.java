package brucehan.auth.application;

import brucehan.auth.config.exception.ApplicationException;
import brucehan.auth.config.exception.ApplicationExceptionType;
import brucehan.auth.domain.entity.RefreshTokenEntity;
import brucehan.auth.domain.repository.RefreshTokenRedisRepository;
import brucehan.auth.infrastructure.kakao_client.dto.JwtTokenDto;
import brucehan.auth.presentation.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static brucehan.auth.config.exception.ApplicationExceptionType.*;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final JwtProvider jwtProvider;
    private final RefreshTokenRedisRepository refreshTokenRepository;

    public JwtTokenDto reissueTokens(String refreshToken) {
        Long memberId = jwtProvider.getMemberId(refreshToken);
        RefreshTokenEntity entity = refreshTokenRepository.findById(memberId.toString())
                .orElseThrow(() -> new ApplicationException(JWT_REFRESH_NOT_FOUND_IN_REDIS));
        entity.validateRefreshToken(refreshToken);

        JwtTokenDto newTokens = jwtProvider.generateTokens(memberId);
        saveRefreshToken(memberId, newTokens.refreshToken());
        return newTokens;
    }

    public void saveRefreshToken(Long memberId, String refreshToken) {
        RefreshTokenEntity entity = RefreshTokenEntity.create(memberId.toString(), refreshToken);
        refreshTokenRepository.save(entity);
    }
}
