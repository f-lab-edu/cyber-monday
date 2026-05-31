package brucehan.auth.middleware.handler;

import brucehan.auth.domain.entity.MemberEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class OAuthAttribute {
    private final Map<String, Object> attributes;
    private final String email;
    private final String picture;
    private final String provider;
    private final String sub;

    public static OAuthAttribute of(String provider, Map<String, Object> attributes) {
        switch (provider) {
            case "kakao" -> {
                return ofKakao(attributes);
            }
            case "google" -> {
                throw new UnsupportedOperationException("Google OauthClient is not implemented");
            }
            default -> throw new IllegalArgumentException("Unknown provider " + provider);
        }
    }

    private static OAuthAttribute ofKakao(Map<String, Object> attributes) {
        String email = String.valueOf(attributes.get("email"));
        String picture = String.valueOf(attributes.getOrDefault("picture", null));
        String sub = String.valueOf(attributes.get("sub"));
        return new OAuthAttribute(attributes, email, picture, "kakao", sub);
    }

    public MemberEntity toMemberEntity() {
        return MemberEntity.builder()
                .nickname(String.valueOf(attributes.get("nickname")))
                .email(email)
                .provider(provider)
                .subject(sub)
                .build();
    }
}
