package brucehan.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OauthProvider {
    KAKAO("KAKAO"),
    GOOGLE("GOOGLE");

    private final String provider;
}
