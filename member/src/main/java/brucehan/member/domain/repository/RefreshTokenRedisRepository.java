package brucehan.member.domain.repository;

import brucehan.member.domain.entity.RefreshTokenEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * TODO 질문
 * 1. Refresh Token이 탈취됐을 때 강제 무효화하는 게 얼마나 중요한가? -> 중요하긴 함. 보안상 탈취되고서 accesstoken 을 해커가 재발급받고 해커가 지맘대로 결제해버리고 그러면 안 되긴 함.
 * 얼마나?는 무슨 뜻일까 -> 질문
 * 2. 다중 디바이스 로그인을 지원할 건가? -> 아니오. 난이도 높지 않게 단일 디바이스 먼저 해보겠음
 * 3. 운영 환경에서 Redis가 멈추면 로그인이 멈춰도 괜찮은가? -> 다시 로그인하면 되긴 한데,,, 크기에 따라 다를듯. 영향도가 젤 고민 -> 질문
 */
@Repository
public interface RefreshTokenRedisRepository extends CrudRepository<RefreshTokenEntity, String> {

}
