package brucehan.member.domain.entity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheKey {
    public static final String OIDC_PUBLIC_KEYS = "oidc:public:keys";
}