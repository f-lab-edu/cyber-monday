package brucehan.auth.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheKey {
    public static final String OIDC_PUBLIC_KEYS = "oidc:public:keys";
}