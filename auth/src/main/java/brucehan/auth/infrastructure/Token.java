package brucehan.auth.infrastructure;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;

@Getter
@RequiredArgsConstructor
public class Token {
    private final String accessToken;
    private final String refreshToken;

    public static Token create(String accessToken, String refreshToken) {
        return new Token(accessToken, refreshToken);
    }

    public ResponseCookie.ResponseCookieBuilder createAccessTokenCookie() {
        return ResponseCookie.from("accessToken", accessToken);
    }
    public ResponseCookie.ResponseCookieBuilder createRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", refreshToken);
    }
}
