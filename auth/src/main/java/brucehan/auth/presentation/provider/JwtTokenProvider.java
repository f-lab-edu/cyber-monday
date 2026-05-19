package brucehan.auth.presentation.provider;

import brucehan.auth.infrastructure.kakao_client.dto.JwtProperties;
import brucehan.auth.infrastructure.kakao_client.dto.JwtTokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;
    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    // accessToken, refreshToken 생성
    public JwtTokenDto generateTokens(Long memberId) {
        LocalDateTime now = LocalDateTime.now();
        String accessToken = generateToken(memberId, now, jwtProperties.getAccessTokenValidityInSeconds());
        String refreshToken = generateToken(memberId, now, jwtProperties.getRefreshTokenValidityInSeconds());
        return new JwtTokenDto(accessToken, refreshToken);
    }

    // 토큰 검증 (유효 기간이 지나지 않은 토큰인지)
    public boolean validateToken(String token) {
        Jws<Claims> claims = getClaims(token);
        return !claims.getPayload().getExpiration().before(new Date());
    }

    public Long getMemberIdFromToken(String token) {
        Jws<Claims> claims = getClaims(token);
        return Long.parseLong(claims.getPayload().getSubject());
    }

    // 토큰 생성
    private String generateToken(Long memberId, LocalDateTime now, long validityInSeconds) {
        LocalDateTime expiration = now.plusSeconds(validityInSeconds);
        return Jwts.builder()
                .subject(memberId.toString())
                .issuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .expiration(Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(jwtProperties.getKey())
                .compact();
    }

    /**
     * token에서 claim 정보 추출하기
     * TODO Public or Secret key 구하는 로직으로 수정
     * @param token
     * @return
     */
    private Jws<Claims> getClaims(String token) {
        return Jwts.parser()
                .verifyWith((PublicKey) jwtProperties.getKey())
                .build()
                .parseSignedClaims(token);
    }

}
