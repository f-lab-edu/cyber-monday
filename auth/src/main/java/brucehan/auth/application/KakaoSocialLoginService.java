package brucehan.auth.application;

import brucehan.auth.application.helper.OidcTokenVerification;
import brucehan.auth.domain.OauthProvider;
import brucehan.auth.domain.entity.MemberEntity;
import brucehan.auth.domain.repository.MemberRepository;
import brucehan.auth.infrastructure.kakao_client.dto.JwtTokenDto;
import brucehan.auth.infrastructure.kakao_client.dto.Member;
import brucehan.auth.infrastructure.kakao_client.dto.OidcPayload;
import brucehan.auth.infrastructure.kakao_client.dto.PublicKeysDto;
import brucehan.auth.infrastructure.kakao_client.dto.request.KakaoSocialLoginRequest;
import brucehan.auth.infrastructure.kakao_client.dto.response.KakaoOAuthUserResponse;
import brucehan.auth.middleware.handler.OauthClientFactory;
import brucehan.auth.presentation.provider.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoSocialLoginService {

    private final OauthClientFactory oauthClientFactory;
    private final OidcTokenVerification oidcTokenVerification;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    // TODO KakaoOAuthUserResponse, KakaoAccessTokenResponse를 반환할지 말지 고민
    public JwtTokenDto loginOrSignUp(KakaoSocialLoginRequest request) {
        OauthClient oauthClient = oauthClientFactory.getClient(request.provider());
        String idToken = oauthClient.getTokens(request.code()).idToken();
        PublicKeysDto publicKeys = oauthClient.getPublicKeys();
        OidcPayload oidcPayload = oidcTokenVerification.verifyIdToken(idToken, publicKeys);

        Member member = memberRepository.findByProviderAndSubject(request.provider(), oidcPayload.subject())
                .map(Member::fromEntity) // TODO fromEntity는 왜 static일까?
                .orElseGet(() -> createMember(oidcPayload, request.provider()));

        Long memberId = Objects.requireNonNull(member.id(), "Member id must not be null");
        JwtTokenDto tokens = jwtProvider.generateTokens(memberId);
        refreshTokenService.saveRefreshToken(memberId, tokens.refreshToken());
        return tokens;
    }

    private Member createMember(OidcPayload oidcPayload, OauthProvider provider) {
        Member draft = new Member(
                null, // TODO UUIDv7로 만들어야 함
                oidcPayload.subject(),
                oidcPayload.email()
                // TODO provider도 넣어야 함. SocialAccount를 참고할 것
        );

        MemberEntity saved = memberRepository.save(draft.toEntity());
        return Member.fromEntity(saved);
    }

}
