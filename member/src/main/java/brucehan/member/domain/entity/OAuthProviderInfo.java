package brucehan.member.domain.entity;

import brucehan.member.domain.OauthProvider;

/**
 * @param provider
 * @param subject
 */
public record OAuthProviderInfo(
        OauthProvider provider,
        String subject
) {
}
