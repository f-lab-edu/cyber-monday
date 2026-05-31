package brucehan.auth.infrastructure.service;

import brucehan.auth.domain.entity.MemberEntity;
import brucehan.auth.domain.repository.MemberRepository;
import brucehan.auth.middleware.handler.OAuthAttribute;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {
    private static final String DEFAULT_ROLE = "ROLE_USER";
    protected final MemberRepository memberRepository;

    public OAuthAttribute getUserInfo(OidcUserRequest userRequest, OAuth2User oauth2User) {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        return OAuthAttribute.of(provider, oauth2User.getAttributes());
    }
}
