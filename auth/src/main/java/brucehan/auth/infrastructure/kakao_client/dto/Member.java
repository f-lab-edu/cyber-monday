package brucehan.auth.infrastructure.kakao_client.dto;

import brucehan.auth.domain.OauthProvider;
import brucehan.auth.domain.entity.MemberEntity;

public record Member(
        Long id,
        String nickname,
        String email
) {
    public static Member fromEntity(MemberEntity entity) {
        return new Member(
                entity.getMemberId(),
                entity.getNickname(),
                entity.getEmail()
        );
    }

    public MemberEntity toEntity() {
        return new MemberEntity(id, nickname, email);
    }
}
