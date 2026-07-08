package brucehan.member.presentation;

import brucehan.member.application.MemberService;
import brucehan.member.domain.entity.MemberEntity;
import brucehan.member.presentation.response.MemberResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/v1/username")
    public MemberResponse getUsername(@AuthenticationPrincipal MemberEntity member) {
        log.info("memberEntity : {} : {} : {}", member.getEmail(), member.getProvider(), member.getRole());
        log.info("memberEntity : {} : {}", member.getSubject(), member.getNickname());
        MemberResponse info = memberService.getMyInfo(member);
        log.info("info : {}", info);
        return info;
    }
}
