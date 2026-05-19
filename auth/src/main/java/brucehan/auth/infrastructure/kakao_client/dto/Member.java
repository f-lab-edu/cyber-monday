package brucehan.auth.infrastructure.kakao_client.dto;

import brucehan.auth.domain.OauthProvider;
import brucehan.auth.domain.entity.MemberEntity;
import brucehan.auth.domain.entity.OAuthProviderInfo;
import lombok.Getter;

@Getter
public class Member {

    private Long memberId;
    private String name;
    private String email;
    private String picture;
    private OAuthProviderInfo oAuthProviderInfo;

    public Member(
            String nickname,
            String email
    ) {
        this.name = nickname;
        this.email = email;
    }

    public Member(String name, String email, String picture, OAuthProviderInfo oAuthProviderInfo) {
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.oAuthProviderInfo = oAuthProviderInfo;
    }

    public static Member fromEntity(MemberEntity entity) {
        return new Member(
                entity.getNickname(),
                entity.getEmail()
        );
    }

    public MemberEntity toEntity() {
        return new MemberEntity(name, email);
    }
}
