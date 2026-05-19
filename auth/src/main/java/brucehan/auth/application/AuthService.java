package brucehan.auth.application;

import brucehan.auth.domain.OauthProvider;
import brucehan.auth.domain.entity.OAuthProviderInfo;
import brucehan.auth.domain.repository.MemberRepository;
import brucehan.auth.infrastructure.kakao_client.dto.*;
import brucehan.auth.middleware.handler.MockOAuthClientFactory;
import brucehan.auth.presentation.provider.JwtTokenProvider;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final MockOAuthClientFactory oAuthClientFactory;
    private final OIDCTokenVerification oidcTokenVerification;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthService(MockOAuthClientFactory oAuthClientFactory,
                       OIDCTokenVerification oidcTokenVerification,
                       MemberRepository memberRepository,
                       JwtTokenProvider jwtTokenProvider,
                       RefreshTokenService refreshTokenService) {
        this.oAuthClientFactory = oAuthClientFactory;
        this.oidcTokenVerification = oidcTokenVerification;
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public JwtTokenDto authenticateAndRegister(LoginRequest request) {
        // id token 요청
        OAuthClient oAuthClient = oAuthClientFactory.getClientOld(request.provider());
        String idToken = oAuthClient.getToken(request.authCode()).getIdToken();

        // 공개키 요청
        OIDCPublicKeyList publicKeys = oAuthClient.getPublicKeys();

        // id token 파싱
        OidcPayload oidcPayload = oidcTokenVerification.verifyIdToken(idToken, publicKeys);

        // 유저 존재 여부 확인 -> 없으면 유저 생성 (= 자동 회원가입)
        Member member = memberRepository
                .findByProviderAndSubject(request.provider().getProvider(), oidcPayload.subject())
                .map(Member::fromEntity)
                .orElseGet(() -> createMember(oidcPayload, request.provider()));

        // JWT 토큰 생성 & 반환
        Long memberId = member.getMemberId();
        if (memberId == null) {
            throw new IllegalStateException("Member ID must not be null");
        }
        JwtTokenDto tokens = jwtTokenProvider.generateTokens(memberId);

        // refresh token 저장
        refreshTokenService.saveRefreshToken(memberId, tokens.refreshToken());
        return tokens;
    }

    private Member createMember(OidcPayload oidcPayload, OauthProvider provider) {
        Member newMember = new Member(
                oidcPayload.name(),
                oidcPayload.email(),
                oidcPayload.picture(),
                new OAuthProviderInfo(provider, oidcPayload.subject())
        );
        return Member.fromEntity(memberRepository.save(newMember.toEntity()));
    }
}
