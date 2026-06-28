package brucehan.member.application;

import brucehan.member.domain.entity.MemberEntity;
import brucehan.member.domain.repository.MemberRepository;
import brucehan.member.presentation.response.MemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberResponse getMyInfo(MemberEntity member) {
        MemberEntity memberEntity = memberRepository.findByEmailAndProvider(member.getEmail(), member.getProvider())
                .orElseThrow(IllegalArgumentException::new);
        return MemberResponse.builder()
                .nickname(memberEntity.getNickname())
                .email(memberEntity.getEmail())
                .profileImage(memberEntity.getProfileImage())
                .roles(memberEntity.getRole())
                .provider(memberEntity.getProvider())
                .build();
    }
}
