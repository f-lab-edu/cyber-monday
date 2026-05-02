package brucehan.auth.domain.repository;

import brucehan.auth.domain.OauthProvider;
import brucehan.auth.domain.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByProviderAndSubject(OauthProvider provider, String subject);
    MemberEntity save(MemberEntity memberInfo);
}
