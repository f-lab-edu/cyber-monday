package brucehan.auth.domain.entity;

import brucehan.auth.domain.OauthProvider;

/**
 * @param provider
 * @param subject
 */
public record OAuthProviderInfo(
        OauthProvider provider,
        String subject
) {
}
