package brucehan.auth.application;

import brucehan.auth.infrastructure.kakao_client.dto.PublicKeysDto;
import brucehan.auth.infrastructure.kakao_client.dto.response.KakaoAccessTokenResponse;

public interface OauthClient {

    /**
     * 인증 코드로 엑세스 토큰과 ID Token(OIDC) 요청하는 메서드
     * @param authCode 인증코드
     * @return 엑세스 토큰과 ID Token(OIDC)
     */
    KakaoAccessTokenResponse getTokens(String authCode);

    /**
     * ID Token을 파싱할 공개키를 가져오는 메서드
     * @return 공개키 정보
     */
    PublicKeysDto getPublicKeys();
}
