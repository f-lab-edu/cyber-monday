package brucehan.auth.infrastructure.kakao_client;

import brucehan.auth.domain.entity.CacheKey;
import brucehan.auth.infrastructure.kakao_client.dto.PublicKeysDto;
import brucehan.auth.infrastructure.kakao_client.dto.response.KakaoAccessTokenResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static brucehan.auth.domain.entity.CacheKey.OIDC_PUBLIC_KEYS;

@FeignClient(value = "kakaoAuthClient", url = "${oauth2.client.kakao.kakao-url}") // TODO : yml로 묶기
public interface KakaoTokenClient {

    // 토큰 요청 https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#request-token
    @PostMapping(
            value= "/oauth/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    KakaoAccessTokenResponse issueToken(
            @RequestParam(name = "code") final String code,
            @RequestParam(name = "client_id") final String clientId,
            @RequestParam(name = "redirect_uri") final String redirectUrl,
            @RequestParam(name = "grant_type") final String grantType,
            @RequestParam(name = "client_secret") final String clientSecret
    );

    @Cacheable(cacheNames = CacheKey.Names.OIDC_PUBLIC_KEYS, cacheManager = "oidcCacheManager")
    @GetMapping("/.well-known/jwks.json")
    PublicKeysDto getKakaoPublicKeys();
}