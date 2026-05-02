package brucehan.auth.infrastructure.kakao_client.dto.request;

import brucehan.auth.domain.OauthProvider;

public record KakaoSocialLoginRequest(
        String code,
        OauthProvider provider,
        String state
) {
}
