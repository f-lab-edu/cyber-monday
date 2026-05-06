package brucehan.auth.domain.entity;

public enum CacheKey {
    OIDC_PUBLIC_KEYS(Names.OIDC_PUBLIC_KEYS);

    private final String keyName;

    CacheKey(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyName() {
        return keyName;
    }

    public static class Names {
        public static final String OIDC_PUBLIC_KEYS = "oidc:public:keys";

        private Names() {}
    }
}