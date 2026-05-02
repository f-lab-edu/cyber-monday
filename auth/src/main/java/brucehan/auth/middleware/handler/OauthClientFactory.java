package brucehan.auth.middleware.handler;

import brucehan.auth.domain.OauthProvider;
import brucehan.auth.application.impl.KakaoOauthClient;
import brucehan.auth.application.OauthClient;
import org.springframework.stereotype.Component;

@Component
public class OauthClientFactory {
    private final KakaoOauthClient kakaoOauthClient;

    public OauthClientFactory(KakaoOauthClient kakaoOauthClient) {
        this.kakaoOauthClient = kakaoOauthClient;
    }

    public OauthClient getClient(OauthProvider provider) {
        return switch (provider) {
            case KAKAO -> kakaoOauthClient;
            case GOOGLE -> throw new UnsupportedOperationException("Google OauthClient is not implemented");
        };
    }
}
