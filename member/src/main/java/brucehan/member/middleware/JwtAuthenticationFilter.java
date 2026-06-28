package brucehan.member.middleware;

import brucehan.member.application.RedisTokenManagementService;
import brucehan.member.domain.entity.MemberEntity;
import brucehan.member.infrastructure.Token;
import brucehan.member.infrastructure.TokenFactory;
import brucehan.member.infrastructure.TokenService;
import brucehan.member.middleware.util.CookieUtils;
import io.jsonwebtoken.lang.Strings;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final TokenService tokenService;
    private final RedisTokenManagementService tokenManagementService;
    private final TokenFactory tokenFactory;

    /**
     * accessToken 만료 && refreshToken 유효 == accessToken 갱신
     * accessToken 만료 && refreshToken 만료 임박 == accessToken 갱신, refreshToken 갱신
     * accessToken 유효 && refreshToken 만료 임박 == accessToken 갱신, refreshToken 갱신
     * accessToken 만료 && refreshToken 만료 == 401
     * accessToken 유효 && refreshToken 만료 == nothing
     * accessToken 유효 && refreshToken 유효 == nothing
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String accessToken = CookieUtils.getAccessToken((HttpServletRequest) request);
        String refreshToken = CookieUtils.getRefreshToken((HttpServletRequest) request);

        Token token = null;
        log.info("requestUrl : {}", ((HttpServletRequest) request).getRequestURL());
        log.info("accessToken : {}", accessToken);

        if (accessToken != null && !tokenManagementService.isAlreadyLogout(accessToken)) {
            if (tokenService.isExpiredToken(accessToken)) {
                log.info("accessToken expired");
                token = tokenService.refreshToken(refreshToken, LocalDateTime.now());
                setTokenCookie((HttpServletResponse) response, token);
            } else if (tokenService.verifyToken(accessToken) && tokenService.isRefreshTokenNearExpiry(refreshToken)) {
                log.info("refreshToken near expiry");
                token = tokenService.refreshToken(refreshToken, LocalDateTime.now());
                setTokenCookie((HttpServletResponse) response, token);
            } else if (tokenService.verifyToken(accessToken)) {
                log.info("token valid");
                token = Token.create(accessToken, refreshToken);
            }
        }

        if (token != null) {
            setAuthentication(token.getAccessToken());
        }
        chain.doFilter(request, response);
    }

    private void setAuthentication(String accessToken) {
        MemberEntity member = tokenService.parseMemberAuthenticationToken(accessToken);
        Authentication auth = getAuthentication(member);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private Authentication getAuthentication(MemberEntity member) {
        return new UsernamePasswordAuthenticationToken(
                member,
                Strings.EMPTY,
                member.initSimpleGrantedAuthorities()
        );
    }

    private void setTokenCookie(HttpServletResponse response, Token token) {
        CookieUtils.setCookie(response, tokenFactory.createAccessTokenCookie(token));
        CookieUtils.setCookie(response, tokenFactory.createRefreshTokenCookie(token));
    }
}
