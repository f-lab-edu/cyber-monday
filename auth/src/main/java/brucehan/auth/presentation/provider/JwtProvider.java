package brucehan.auth.presentation.provider;

import brucehan.auth.config.exception.ApplicationException;
import brucehan.auth.config.exception.ApplicationExceptionType;
import brucehan.auth.infrastructure.kakao_client.dto.JwtProperties;
import brucehan.auth.infrastructure.kakao_client.dto.JwtTokenDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static brucehan.auth.config.exception.ApplicationExceptionType.*;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;

    public JwtTokenDto generateTokens(Long memberId) {
        long accessTokenExpiration = Instant.now().getEpochSecond() + jwtProperties.accessTokenExpiration();
        long refreshTokenExpiration = Instant.now().getEpochSecond() + jwtProperties.refreshTokenExpiration();
        return new JwtTokenDto(
                generateToken(memberId, accessTokenExpiration),
                generateToken(memberId, refreshTokenExpiration)
        );
    }

    private String generateToken(Long memberId, long expiration) {
        return Jwts.builder()
                .subject(memberId.toString())
                .issuedAt(new Date())
                .expiration(new Date(expiration))
                .signWith(jwtProperties.secretKey())
                .compact();
    }

    public Long getMemberId(String token) {
        Claims claims = getClaims(token).getPayload();
        return Long.valueOf(claims.getSubject());
    }

    private Jws<Claims> getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(jwtProperties.secretKey())
                    .build()
                    .parseSignedClaims(token);
        } catch (SecurityException e) {
            throw new ApplicationException(e, JWT_INVALID_SIGNATURE);
        } catch (ExpiredJwtException e) {
            throw new ApplicationException(e, JWT_EXPIRED);
        } catch (JwtException e) {
            throw new ApplicationException(e, JWT_MALFORMED);
        } catch (Exception e) {
            throw new ApplicationException(e, UNDEFINED_EXCEPTION);
        }
    }
}
