package brucehan.auth.infrastructure.kakao_client.dto;

import brucehan.auth.config.exception.ApplicationException;

import java.io.Serializable;
import java.util.List;

import static brucehan.auth.config.exception.ApplicationExceptionType.OIDC_PUBLIC_KEY_NOT_FOUND;

public record PublicKeysDto(
        List<JWK> keys
) {
    public JWK getMatchedKey(String kid, String alg) {
        return keys.stream()
                .filter(key -> key.kid.equals(kid) && key.alg.equals(alg))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(OIDC_PUBLIC_KEY_NOT_FOUND));
    }

    public record JWK(
            String kid, /* 키 타입 (예: RSA) */
            String kty, /* 공개 키 ID */
            String alg, /* 사용 목적 (예: sig) */
            String use, /* 알고리즘 (예: RS256) */
            String n,   /* RSA 모듈러스 값 */
            String e    /* RSA 지수 값 */
    ) implements Serializable {
    }
}
