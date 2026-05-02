package brucehan.auth.infrastructure.kakao_client.dto;

public record JwtTokenDto(
        String accessToken,
        String refreshToken
) {
}
