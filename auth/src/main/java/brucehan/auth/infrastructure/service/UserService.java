package brucehan.auth.infrastructure.service;

import brucehan.auth.domain.entity.MemberEntity;
import brucehan.auth.domain.repository.MemberRepository;
import brucehan.auth.middleware.handler.OAuthClientFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RequiredArgsConstructor
public class UserService {
    private static final String DEFAULT_ROLE = "ROLE_USER";
    private final MemberRepository memberRepository;

    public OAuthClientFactory getUserInfo(OidcUserRequest userRequest, OAuth2User oauth2User) {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        return OAuthClientFactory.of(provider, oauth2User.getAttributes());
    }

    public MemberEntity saveOrUpdate(OAuthClientFactory attributes) {
        MemberEntity memberEntity = attributes.getMemberFrom(memberRepository)
                .orElseGet(MemberEntity::new);
        attributes.updateProfileUrlIfAbsent(memberEntity);
        return memberRepository.save(memberEntity);
    }
}
