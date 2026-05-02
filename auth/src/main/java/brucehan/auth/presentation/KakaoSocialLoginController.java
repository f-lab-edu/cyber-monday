package brucehan.auth.presentation;

import brucehan.auth.application.RefreshTokenService;
import brucehan.auth.infrastructure.kakao_client.dto.JwtTokenDto;
import brucehan.auth.infrastructure.kakao_client.dto.RefreshRequest;
import brucehan.auth.infrastructure.kakao_client.dto.request.KakaoSocialLoginRequest;
import brucehan.auth.application.KakaoSocialLoginService;
import brucehan.auth.infrastructure.kakao_client.dto.response.KakaoOAuthUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kakao")
public class KakaoSocialLoginController {
    private final KakaoSocialLoginService kakaoSocialLoginService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/sign-up/openfeign")
    public ResponseEntity<JwtTokenDto> loginOrSignUp(
            @RequestBody KakaoSocialLoginRequest kakaoSocialLoginRequest
    ) {
        JwtTokenDto tokens = kakaoSocialLoginService.loginOrSignUp(kakaoSocialLoginRequest);
        return ResponseEntity.ok(tokens);
    }

    @GetMapping("/login/callback")
    public void callback(
            @RequestParam String code,
            @RequestParam String state
    ) {
        System.out.println("code = " + code);
        System.out.println("state = " + state);
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<JwtTokenDto> refreshAccessToken(@RequestBody RefreshRequest request) {
        JwtTokenDto newTokens = refreshTokenService.reissueTokens(request.refreshToken());
        return ResponseEntity.ok(newTokens);
    }

}
