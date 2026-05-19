package brucehan.auth.infrastructure.kakao_client.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * https://developers.kakao.com/docs/ko/kakaologin/rest-api#request-token-response
 */
@Getter
public class OAuthTokenResponse {

        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("id_token")
        private String idToken;

        @JsonProperty("expires_in")
        private Integer expiresIn;

        @JsonProperty("refresh_token")
        private String refreshToken;

        @JsonProperty("refresh_token_expires_in")
        private Integer refreshTokenExpiresIn;

        public OAuthTokenResponse(String accessToken, String idToken, Integer expiresIn, String refreshToken, Integer refreshTokenExpiresIn) {
                this.accessToken = accessToken;
                this.idToken = idToken;
                this.expiresIn = expiresIn;
                this.refreshToken = refreshToken;
                this.refreshTokenExpiresIn = refreshTokenExpiresIn;
        }
}
