package brucehan.auth.presentation.provider;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.authenticator.SavedRequest;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Slf4j
@Component
public class JwtOidcProvider {

    private final String KID = "kid";
    private final String RSA = "RSA";

    // kid는 공개키 목록에서 씀
    public String getKidFromUnsignedTokenHeader(String token, String iss, String aud) {
        return (String) getUnsignedTokenClaims(token, iss, aud).getHeader().get(KID);
    }

    private Jwt<Header, Claims> getUnsignedTokenClaims(String token, String iss, String aud) {
        log.info("token = {}", token);
        log.info("iss = {}", iss);
        log.info("aud = {}", aud);
        try {
            return Jwts.parser()
                    .requireAudience(aud) // aud (카카오톡 어플리케이션 아이디)가 같은지 확인
                    .requireIssuer(iss) // iss(ID 토큰을 발급한 인증 기관 정보)가 카카오인지 확인
                    .build()
                    .parseClaimsJwt(getUnsignedToken(token));
        } catch (ExpiredJwtException eje) { // 파싱하면서 만료된 토큰인지 확인
            throw new RuntimeException();
        } catch (Exception e) {
            log.error(e.toString());
            throw new RuntimeException();
        }
    }

    // 페이로드 가져오기
    private String getUnsignedToken(String token) {
        String[] splitToken = token.split("\\.");
        if (splitToken.length != 3) throw new RuntimeException();
        return splitToken[0] + "." + splitToken[1] + ".";
    }

    public OidcDecodePayload getOidcTokenBody(
            String token,
            String modulus,
            String exponent
    ) {
        Claims body = getOidcTokenJws(token, modulus, exponent).getPayload();
        log.info("aud - {}", body.getAudience());
        return new OidcDecodePayload(
                body.getIssuer(),
                body.getAudience().iterator().next(),
                body.getSubject(),
                body.get("email", String.class)
        );
    }

    // 공개키로 토큰 검증 시도
    public Jws<Claims> getOidcTokenJws(String token, String modulus, String exponent) {
        try {
            log.info("token = {}", token);
            log.info("modulus = {}", modulus);
            log.info("exponent = {}", exponent);
            return Jwts.parser()
                    .verifyWith(getRsaPublicKey(modulus, exponent))
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            log.error("InvalidKeySpecException - {}", e.toString());
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            log.error("NoSuchAlgorithmException - {}", e.toString());
            throw new RuntimeException(e);
        }
    }

    private SecretKey getRsaPublicKey(String modulus, String exponent)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance(RSA);
        byte[] decodeN = Base64.getUrlDecoder().decode(modulus);
        byte[] decodeE = Base64.getUrlDecoder().decode(exponent);
        BigInteger n = new BigInteger(1, decodeN);
        BigInteger e = new BigInteger(1, decodeE);

        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(n, e);
        return (SecretKey) keyFactory.generatePublic(keySpec);
    }
}
