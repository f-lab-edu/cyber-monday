package brucehan.member.middleware.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

public class CookieUtils {
    private CookieUtils() {
        throw new IllegalStateException("Utility class");
    }


    public static String getTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static void setCookie(HttpServletResponse response, ResponseCookie refreshToken) {
        Cookie cookie = new Cookie(refreshToken.getName(), refreshToken.getValue());
        cookie.setDomain(refreshToken.getDomain());
        cookie.setPath(refreshToken.getPath());
        cookie.setSecure(refreshToken.isSecure());
        cookie.setHttpOnly(refreshToken.isHttpOnly());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
