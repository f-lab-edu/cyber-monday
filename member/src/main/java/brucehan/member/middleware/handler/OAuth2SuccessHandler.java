package brucehan.member.middleware.handler;

import brucehan.member.domain.entity.MemberEntity;
import brucehan.member.infrastructure.Token;
import brucehan.member.infrastructure.TokenFactory;
import brucehan.member.infrastructure.TokenService;
import brucehan.member.infrastructure.kakao_client.dto.JwtProperties;
import io.jsonwebtoken.Claims;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final String loginSuccessUri;
    private final TokenService tokenService;
    private final TokenFactory tokenFactory;
    private final JwtProperties jwtProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        final OAuth2User oAuth2User;
        // 1. authentication.getPrincipal()로 Oidc 꺼내기
        final Object principal = authentication.getPrincipal();
        log.info("principal : {}", principal);
        if (principal instanceof DefaultOAuth2User defaultOAuth2User) {
            oAuth2User = defaultOAuth2User;
        } else {
            oAuth2User = (OAuth2User) principal;
        }
        log.info("oAuth2User : {}", oAuth2User);
        // 2. 거기서 도메인 정보 (memberId, email, role 등) 추출
        final MemberEntity member = MemberEntity.toMemberEntityBy(oAuth2User);
        log.info("member : {}", member);
        
        final Token token = generateToken(member, LocalDateTime.now());
        log.info("token : {}", token);

        final Object redirectObj = request.getSession().getAttribute("redirect_url");
        String redirectUrl;
        if (redirectObj == null) {
            redirectUrl = loginSuccessUri;
        } else {
            redirectUrl = String.valueOf(redirectObj);
        }

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam("success", "true")
                .build()
                .toUriString();

        // 3. 자체 JWT 발급 (TokenService 같은 거 만들어야 함)
        setCookie(response, tokenFactory.createAccessTokenCookie(token));
        setCookie(response, tokenFactory.createRefreshTokenCookie(token));

        log.info("Member {} has successfully logged", member.getNickname());

        // 4. JWT를 응답으로 내려보내기 + loginSuccessUri로 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void setCookie(HttpServletResponse response, ResponseCookie cookie) {
        Cookie cookie1 = new Cookie(cookie.getName(), cookie.getValue());
        cookie1.setDomain(cookie.getDomain());
        cookie1.setPath(cookie.getPath());
        cookie1.setSecure(cookie.isSecure());
        cookie1.setHttpOnly(cookie.isHttpOnly());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private Token generateToken(MemberEntity member, LocalDateTime now) {
        Claims claims = tokenService.generateClaims(member);
        String accessToken = tokenService.generateAccessToken(claims, now);
        String refreshToken = tokenService.generateRefreshToken(claims, now);
        return Token.create(accessToken, refreshToken);
    }


}
