package brucehan.member.infrastructure;

public interface TokenManagementService {
    boolean isAlreadyLogout(String accessToken);

    void banAccessToken(String token);
    void banRefreshToken(String token);
}
