package brucehan.auth.infrastructure.kakao_client.dto;

public record HeaderInfo(
        String kid,
        String alg
) {
}
