package brucehan.member.domain.repository;

import brucehan.member.domain.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    MemberEntity save(MemberEntity memberInfo);
    Optional<MemberEntity> findByEmailAndProvider(String email, String provider);
}
