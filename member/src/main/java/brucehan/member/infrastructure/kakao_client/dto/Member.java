package brucehan.member.infrastructure.kakao_client.dto;

import brucehan.member.domain.entity.MemberEntity;
import brucehan.member.domain.entity.OAuthProviderInfo;
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
}
