package brucehan.auth.domain.repository;

import brucehan.auth.domain.OauthProvider;
import brucehan.auth.domain.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByProviderAndSubject(String provider, String subject);
    MemberEntity save(MemberEntity memberInfo);
    Optional<MemberEntity> findByEmailAndProvider(String email, String provider);
}
