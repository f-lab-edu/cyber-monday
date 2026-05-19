package brucehan.auth.presentation;

import brucehan.auth.application.AuthService;
import brucehan.auth.application.RefreshTokenService;
import brucehan.auth.infrastructure.kakao_client.dto.JwtTokenDto;
import brucehan.auth.infrastructure.kakao_client.dto.LoginRequest;
import brucehan.auth.infrastructure.kakao_client.dto.RefreshRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    // 소셜 로그인 후 JWT access, refresh 토큰 반환
    @PostMapping("/login")
    public ResponseEntity<JwtTokenDto> socialLogin(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
    ) {
        JwtTokenDto tokens = authService.authenticateAndRegister(loginRequest);
        return ResponseEntity.ok(tokens);
    }

    // refresh 토큰을 이용하여 access, refresh 토큰 재발급
    public JwtTokenDto refreshAccessToken(@Valid @RequestBody RefreshRequest refreshRequest) {
        return refreshTokenService.reissueTokens(refreshRequest.refreshToken());
    }
}
