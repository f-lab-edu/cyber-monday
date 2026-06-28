package brucehan.member.presentation.response;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class MemberResponse {
    private String nickname;
    private String email;
    private String profileImage;
    private Set<String> roles;
    private String provider;
}
