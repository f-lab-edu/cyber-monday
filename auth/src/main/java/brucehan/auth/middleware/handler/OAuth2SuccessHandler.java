package brucehan.auth.middleware.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private String loginSuccessUri;

    public OAuth2SuccessHandler(String loginSuccessUri) {
        this.loginSuccessUri = loginSuccessUri;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 1. authentication.getPrincipal()로 Oidc 꺼내기
        // 2. 거기서 도메인 정보 (memberId, email, role 등) 추출
        // 3. 자체 JWT 발급 (TokenService 같은 거 만들어야 함)
        // 4. JWT를 응답으로 내려보내기 + loginSuccessUri로 리다이렉트
    }
}
