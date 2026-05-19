package brucehan.auth.infrastructure.kakao_client;

import brucehan.auth.infrastructure.kakao_client.dto.OIDCPublicKeyList;
import brucehan.auth.infrastructure.kakao_client.dto.response.OAuthTokenResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static brucehan.auth.domain.entity.CacheKey.OIDC_PUBLIC_KEYS;

@FeignClient(
        name = "kakaoAuthClient",
        url = "https://kauth.kakao.com"
)
public interface KakaoFeignClient {
    /**
     * 인가 코드로 카카오 인증 서버에 ID Token 요청하기
     */
    @PostMapping("/oauth/token")
    OAuthTokenResponse getToken(
            @RequestParam(value = "grant_type", defaultValue = "authorization_code") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("code") String code,
            @RequestParam("client_secret") String clientSecret
    );

    /**
     * 카카오 인증 서버가 ID 토큰 서명 시 사용한 공개키 목록을 조회
     * 조회 결과 Redis에 캐시
     */
    @GetMapping("/.well-known/jwks.json")
    @Cacheable(value = OIDC_PUBLIC_KEYS, key = "'kakao'")
    OIDCPublicKeyList getPublicKeys();
}
