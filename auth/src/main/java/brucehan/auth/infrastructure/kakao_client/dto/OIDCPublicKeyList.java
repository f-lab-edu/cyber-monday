package brucehan.auth.infrastructure.kakao_client.dto;

import brucehan.auth.config.exception.ApplicationException;
import brucehan.auth.config.exception.ApplicationExceptionType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public record OIDCPublicKeyList(
        @JsonProperty("keys") List<OIDCPublicKey> keys
) implements Serializable {

    /**
     * 주어진 kid(Key ID)와 alg(알고리즘)에 해당하는 OIDC 공개키를 찾아 반환
     */
    public OIDCPublicKey getMatchedKey(String kid, String alg) {
        return keys.stream()
                .filter(key -> key.kid().equals(kid) && key.alg().equals(alg))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(
                        ApplicationExceptionType.OIDC_PUBLIC_KEY_NOT_FOUND
                ));
    }
}
