package brucehan.auth.infrastructure.kakao_client.dto;

import java.time.LocalDateTime;

/**
 * ID 토큰에서 추출한 유저 정보를 담는 레코드
 * @param subject
 * @param email
 * @param picture
 * @param name
 */
public record OidcPayload(
        String subject,
        String email,
        String picture,
        String name
) {
}
