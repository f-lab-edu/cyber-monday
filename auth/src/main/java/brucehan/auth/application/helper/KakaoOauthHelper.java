package brucehan.auth.application.helper;

import brucehan.auth.infrastructure.kakao_client.KakaoTokenClient;
import brucehan.auth.infrastructure.kakao_client.dto.PublicKeysDto;
import brucehan.auth.infrastructure.kakao_client.dto.OidcDecodePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOauthHelper {

    private final KakaoTokenClient kakaoTokenClient;
    private final OauthOidcHelper oauthOidcHelper;

    @Value("${oauth2.client.kakao.client-id}")
    private String aud;

    @Value("${oauth2.client.kakao.kakao-url}")
    private String iss;

    public OidcDecodePayload getOidcDecodePayload(String token) {
        // 캐싱된 공개키 목록 조회
        PublicKeysDto publicKeysDto = kakaoTokenClient.getKakaoPublicKeys();
        log.info("publicKeysDto - {}", publicKeysDto);
        return oauthOidcHelper.getPayloadFromIdToken(
                token, // IdToken
                iss, // iss와 대응되는 값, 카카오 기본 url
                aud, // aud와 대응되는 값, 카카오 app id
                publicKeysDto
        );
    }

}
