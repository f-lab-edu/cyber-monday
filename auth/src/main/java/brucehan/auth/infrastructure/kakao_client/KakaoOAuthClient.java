package brucehan.auth.infrastructure.kakao_client;

import brucehan.auth.application.OAuthClient;
import brucehan.auth.infrastructure.kakao_client.dto.KakaoProperties;
import brucehan.auth.infrastructure.kakao_client.dto.OIDCPublicKeyList;
import brucehan.auth.infrastructure.kakao_client.dto.response.OAuthTokenResponse;
import org.springframework.stereotype.Component;

@Component
public class KakaoOAuthClient implements OAuthClient {

    private final KakaoFeignClient kakaoFeignClient;
    private final KakaoProperties kakaoProperties;

    public KakaoOAuthClient(
            KakaoFeignClient kakaoFeignClient,
            KakaoProperties kakaoProperties
    ) {
        this.kakaoFeignClient = kakaoFeignClient;
        this.kakaoProperties = kakaoProperties;
    }

    @Override
    public OAuthTokenResponse getToken(String authCode) {
        return kakaoFeignClient.getToken(
                "authorization_code",
                kakaoProperties.clientId(),
                kakaoProperties.redirectUri(),
                authCode,
                kakaoProperties.clientSecret()
        );
    }

    @Override
    public OIDCPublicKeyList getPublicKeys() {
        return kakaoFeignClient.getPublicKeys();
    }
}
