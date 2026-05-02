package brucehan.auth.application.impl;

import brucehan.auth.application.OauthClient;
import brucehan.auth.infrastructure.kakao_client.KakaoTokenClient;
import brucehan.auth.infrastructure.kakao_client.dto.PublicKeysDto;
import brucehan.auth.infrastructure.kakao_client.dto.response.KakaoAccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KakaoOauthClient implements OauthClient {

    @Value("${oauth2.client.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth2.client.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${oauth2.client.kakao.grant-type}")
    private String kakaoGrantType;

    @Value("${oauth2.client.kakao.client-secret}")
    private String kakaoClientSecret;

    private final KakaoTokenClient tokenClient;

    public KakaoOauthClient(KakaoTokenClient tokenClient) {
        this.tokenClient = tokenClient;
    }

    @Override
    public KakaoAccessTokenResponse getTokens(String authCode) {
        return tokenClient.issueToken(
                authCode,
                kakaoClientId,
                kakaoRedirectUri,
                kakaoGrantType,
                kakaoClientSecret
        );
    }

    @Override
    public PublicKeysDto getPublicKeys() {
        return tokenClient.getKakaoPublicKeys();
    }
}
