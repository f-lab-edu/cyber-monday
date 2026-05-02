package brucehan.auth.domain.repository;

import brucehan.auth.domain.entity.RefreshTokenEntity;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRedisRepository extends CrudRepository<RefreshTokenEntity, String> {

}
