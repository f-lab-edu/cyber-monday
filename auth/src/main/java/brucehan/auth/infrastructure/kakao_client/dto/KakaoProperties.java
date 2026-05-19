package brucehan.auth.infrastructure.kakao_client.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao")
public record KakaoProperties(
        String clientId,
        String clientSecret,
        String redirectUri
) {
}
