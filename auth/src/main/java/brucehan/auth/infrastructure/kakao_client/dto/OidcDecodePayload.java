package brucehan.auth.infrastructure.kakao_client.dto;

public record OidcDecodePayload(
        String iss,
        String aud,
        String sub,
        String email
) {

}
