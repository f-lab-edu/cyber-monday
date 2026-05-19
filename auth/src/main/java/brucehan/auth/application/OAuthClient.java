package brucehan.auth.application;

import brucehan.auth.infrastructure.kakao_client.dto.OIDCPublicKeyList;
import brucehan.auth.infrastructure.kakao_client.dto.response.OAuthTokenResponse;

public interface OAuthClient {

    // 인증 코드로 엑세스 토큰 & ID token 요청하는 메서드
    OAuthTokenResponse getToken(String authCode);

    // ID token을 파싱할 공개 키 리스트를 가져오는 메서드
    OIDCPublicKeyList getPublicKeys();
}
