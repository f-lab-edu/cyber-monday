package brucehan.auth.infrastructure;

public interface TokenManagementService {
    boolean isAlreadyLogout(String accessToken);

    void banAccessToken(String token);
    void banRefreshToken(String token);
    void clear();
}
