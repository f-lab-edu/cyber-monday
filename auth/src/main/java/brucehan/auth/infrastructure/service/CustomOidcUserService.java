package brucehan.auth.infrastructure.service;

import brucehan.auth.domain.entity.MemberEntity;
import brucehan.auth.domain.repository.MemberRepository;
import brucehan.auth.middleware.handler.OAuthAttribute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Map;

@Slf4j
public class CustomOidcUserService extends UserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    public CustomOidcUserService(MemberRepository memberRepository) {
        super(memberRepository);
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // 1단계 : 카카오에서 사용자 정보 가져오기 (Spring 위임)
        log.info("Loading OidcUser: {}", userRequest.getIdToken());

        // 이걸 한 거임 https://kauth.kakao.com/oauth/token
        OidcUser oidcUser = new OidcUserService().loadUser(userRequest);

        // 2단계 : 속성에서 본인 도메인 필드 뽑기
//        Map<String, Object> attributes = oidcUser.getAttributes();
//        for (String s : attributes.keySet()) {
//            // TODO 뭐가 출력되는지 보려고 만든 거. 배포 전 지울 예정
//            log.info("s = {}", s);
//            log.info("attribute value = {}", attributes.get(s));
//        }
//        String email = (String) attributes.get("email");
//        String provider = "kakao";
        log.info("여기까지는 통과 {}", oidcUser.getAttributes());
        OAuthAttribute oAuthInfo = getUserInfo(userRequest, oidcUser);

        // 3단계 : 회원 조회/저장
        MemberEntity member = memberRepository.findByEmailAndProvider(oAuthInfo.getEmail(), oAuthInfo.getProvider())
                .orElseGet(oAuthInfo::toMemberEntity);
        member.updateProfileUrlIfAbsent(oAuthInfo.getPicture());
        memberRepository.save(member);

        // 4단계 : OidcUser 다시 만들어서 반환 (DB ID같은 도메인 정보 포함)
        return createOAuth2User(member, userRequest);
    }

    private OidcUser createOAuth2User(MemberEntity memberEntity, OidcUserRequest userRequest) {
        Collection<? extends GrantedAuthority> authorities = memberEntity.getSimpleGrantedAuthorities();
        OidcIdToken idToken = userRequest.getIdToken();
        Map<String, Object> claims = idToken.getClaims();
        Map<String, Object> attributeMap = memberEntity.toAttributeMap();

        attributeMap.putAll(claims);
        log.info("attributeMap : {}", attributeMap);
        OidcUserInfo userInfo = new OidcUserInfo(attributeMap);

        String nameAttributeKey = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        return new DefaultOidcUser(
                authorities,
                userRequest.getIdToken(),
                userInfo,
                nameAttributeKey
        );
    }
}
