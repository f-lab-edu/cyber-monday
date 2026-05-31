package brucehan.auth.infrastructure;

import brucehan.auth.domain.entity.MemberEntity;
import brucehan.auth.infrastructure.kakao_client.dto.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TokenService {
    private static final Duration REFRESH_EXPIRY_IMMINENT_TIME = Duration.ofHours(1);
    private final SecretKey secretKey;
    private final long tokenPeriod;
    private final long refreshPeriod;

    public TokenService(
            @Value("${jwt.secret}") String secretKey,
            JwtProperties jwtProperties
    ) {
            this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            this.tokenPeriod = jwtProperties.getAccessTokenExpiration();
            this.refreshPeriod = jwtProperties.getRefreshTokenExpiration();
    }

    public boolean isExpiredToken(String token) {
        Jws<Claims> claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException ej) {
            log.info("Jwt expired: {}", ej.getMessage());
            return true;
        } catch (JwtException e) {
            log.error("Jwt exception: {}", e.getMessage());
            return false;
        }
        log.info("Get claims: {}", claims);
        return claims.getPayload()
                .getExpiration()
                .before(new Date());
    }

    public Token refreshToken(String refreshToken, LocalDateTime now) {
        if (!verifyToken(refreshToken)) throw new UnsupportedJwtException("expired refresh token");
        MemberEntity member = parseMemberAuthenticationToken(refreshToken);
        Claims claims = generateClaims(member);
        String accessToken = generateAccessToken(claims, now);
        String newRefreshToken = generateRefreshToken(claims, now);
        return Token.create(accessToken, newRefreshToken);
    }

    public String generateAccessToken(Claims claims, LocalDateTime now) {
        LocalDateTime expiration = now.plusSeconds(tokenPeriod);
        return Jwts.builder()
                .claims(claims)
                .subject(claims.getSubject())
                .issuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .expiration(Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(Claims claims, LocalDateTime now) {
        LocalDateTime expiration = now.plusSeconds(refreshPeriod);
        return Jwts.builder()
                .claims(claims)
                .subject(claims.getSubject())
                .issuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .expiration(Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(secretKey)
                .compact();
    }

    public Claims generateClaims(MemberEntity member) {
        Set<String> role = member.getRole();
        if (role == null || role.isEmpty()) {
            role = new HashSet<>();
            role.add("ROLE_USER");
        }
        return Jwts.claims()
                .subject(member.getEmail())
                .add("id", member.getId())
                .add("nickname", member.getNickname())
                .add("provider", member.getProvider())
                .add("profileUrl", member.getProfileUrl())
                .add("roles", String.join(" ", role))
                .build();
    }

    public MemberEntity parseMemberAuthenticationToken(String token) {
        Claims payload = null;
        try {
            payload = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Jwt parse error : {}", e.getMessage());
            throw new BadCredentialsException("Jwt parse error");
        }
        Long id = payload.get("id", Long.class);
        String email = payload.getSubject();
        String nickname = payload.get("nickname", String.class);
        String provider = payload.get("provider", String.class);
        String profileUrl = payload.get("profileUrl", String.class);
        String roles = payload.get("roles", String.class);

        if (roles == null || roles.isBlank()) {
            roles = "ROLE_USER";
        }
        Set<String> roleSet = Arrays.stream(roles.split(" "))
                .collect(Collectors.toSet());
        return new MemberEntity(id, nickname, email, provider, roleSet, profileUrl);
    }

    public boolean verifyToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            log.info("verifyToken - 남은 시간 : {} ", claims.getPayload().getExpiration().getTime() - new Date().getTime());
            return claims.getPayload()
                    .getExpiration()
                    .after(new Date()); // true가 되어야 함
        } catch (ExpiredJwtException e) {
            log.info("verifyToken - Jwt expired: {}", e.getMessage());
            return false; // false가 되어야 밖에 ! 붙어서 true가 됨
        } catch (JwtException | IllegalArgumentException e) {
            log.error("verifyToken - token verify failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isRefreshTokenNearExpiry(String token) {
        Date expiration;
        Jws<Claims> claims;
        try {
             claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("isRefreshTokenNearExpiry - token verify failed: {}", e.getMessage());
            return true;
        }
        expiration = claims.getPayload()
                .getExpiration();

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime expirationDateTime = expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        Duration duration = Duration.between(now, expirationDateTime);
        log.info("Duration : {}" , duration.toMillis());
        log.info("EXPIRATION : {}" , REFRESH_EXPIRY_IMMINENT_TIME.toMillis());
        log.info("Duration compare : {}" , duration.compareTo(REFRESH_EXPIRY_IMMINENT_TIME));
        log.info("거의 만료됐냐 : {}" , duration.compareTo(REFRESH_EXPIRY_IMMINENT_TIME) < 0);
        return duration.compareTo(REFRESH_EXPIRY_IMMINENT_TIME) < 0;
    }
}
