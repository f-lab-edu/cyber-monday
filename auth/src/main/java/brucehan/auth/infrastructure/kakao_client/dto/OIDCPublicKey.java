package brucehan.auth.infrastructure.kakao_client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OIDCPublicKey(
        @JsonProperty("kid") String kid,
        @JsonProperty("kty") String kty,
        @JsonProperty("alg") String alg,
        @JsonProperty("use") String use,
        @JsonProperty("n") String n,
        @JsonProperty("e") String e
) implements Serializable {
}
