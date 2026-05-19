package brucehan.auth.middleware.handler;

import brucehan.auth.domain.OauthProvider;
import brucehan.auth.application.OAuthClient;
import brucehan.auth.domain.entity.MemberEntity;
import brucehan.auth.domain.repository.MemberRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static brucehan.auth.domain.OauthProvider.*;

@Getter
@RequiredArgsConstructor
public class OAuthClientFactory {
    private final Map<String, Object> attributes;
    private final String email;
    private final String profileUrl;
    private final String provider;
    private final String sub;

    @Deprecated
    public static OAuthClient getClientOld(OauthProvider provider) {
        return switch (provider) {
            case KAKAO -> null;
            case GOOGLE -> throw new UnsupportedOperationException("Google OauthClient is not implemented");
        };
    }

    public static OAuthClientFactory of(String provider, Map<String, Object> attributes) {
        switch (provider) {
            case "kakao" -> {
                return getKakaoClient(attributes);
            }
            case "google" -> {
                throw new UnsupportedOperationException("Google OauthClient is not implemented");
            }
            default -> throw new IllegalArgumentException("Unknown provider " + provider);
        }
    }

    private static OAuthClientFactory getKakaoClient(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccountMap = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profileMap = new HashMap<>();
        if (kakaoAccountMap.containsKey("profile")) {
            profileMap = (Map<String, Object>) kakaoAccountMap.get("profile");
        }
        String email = (String) kakaoAccountMap.get("email");
        String profileUrl = (String) profileMap.getOrDefault("profile_image_url", null);
        String sub = (String) attributes.get("id");
        return new OAuthClientFactory(attributes, email, profileUrl, KAKAO.getProvider(), sub);

    }

    public Optional<MemberEntity> getMemberFrom(MemberRepository repository) {
        return repository.findByEmailAndProvider(email, provider)
                .stream().findAny(); // 순서를 보장하지 않는 대신 병렬 처리에서 최대 성능을 냄. 단축 종료 연산
    }

    public void updateProfileUrlIfAbsent(MemberEntity memberEntity) {
        if (memberEntity.getProfileUrl() == null) {
            memberEntity.changeProfileUrl(profileUrl);
        }
    }
}
