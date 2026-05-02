package brucehan.auth.infrastructure.kakao_client.dto;

import java.time.LocalDateTime;

public record OidcPayload(
        String subject,
        String issuer,
        String email,
        LocalDateTime expiration,
        LocalDateTime issuedAt
) {
}
