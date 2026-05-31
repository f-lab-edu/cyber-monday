package brucehan.auth.infrastructure;

import brucehan.auth.middleware.handler.CookieDomainProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
//@RequiredArgsConstructor
public class TokenFactory {
//    private final CookieDomainProvider provider;

    public ResponseCookie createAccessTokenCookie(Token token) {
        return token.createAccessTokenCookie()
                .domain("localhost") // provider.domain()
                .sameSite("None")
                .path("/")
                .secure(true)
                .httpOnly(true)
                .build();
    }

    public ResponseCookie createRefreshTokenCookie(Token token) {
        return token.createRefreshTokenCookie()
                .domain("localhost") // provider.domain
                .sameSite("None")
                .path("/")
                .secure(true)
                .httpOnly(true)
                .build();
    }
}
