package brucehan.auth.application.helper;

import brucehan.auth.infrastructure.kakao_client.dto.PublicKeysDto;
import brucehan.auth.presentation.provider.JwtOidcProvider;
import brucehan.auth.infrastructure.kakao_client.dto.OidcDecodePayload;
import org.springframework.stereotype.Component;

/**
 * OAuth OIDC는 스펙이기 때문에 OauthOidcHelper 하나로 카카오, 구글 등 다 대응할 수 있어야 한다.
 * KakaoOauthHelper 등에서 아래 소스들을 사용한다.
 */
@Component
public record OauthOidcHelper(JwtOidcProvider jwtOidcProvider) {

    public OidcDecodePayload getPayloadFromIdToken(
            String token,
            String iss,
            String aud,
            PublicKeysDto oidcPublicKeysResponse
    ) {
        String kid = getKidFromUnsignedIdToken(token, iss, aud);
        PublicKeysDto.JWK oidcPublicKeyDto =
                oidcPublicKeysResponse.keys().stream()
                        .filter(o -> o.kid().equals(kid))
                        .findFirst()
                        .orElseThrow();

        return jwtOidcProvider.getOidcTokenBody(
                token, oidcPublicKeyDto.n(), oidcPublicKeyDto.e()
        );
    }

    // kid를 토큰에서 가져온다
    private String getKidFromUnsignedIdToken(String token, String iss, String aud) {
        return jwtOidcProvider.getKidFromUnsignedTokenHeader(token, iss, aud);
    }
}
