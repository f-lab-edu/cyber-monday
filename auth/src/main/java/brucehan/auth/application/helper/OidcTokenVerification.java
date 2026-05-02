package brucehan.auth.application.helper;

import brucehan.auth.config.exception.ApplicationException;
import brucehan.auth.config.exception.ApplicationExceptionType;
import brucehan.auth.infrastructure.kakao_client.dto.HeaderInfo;
import brucehan.auth.infrastructure.kakao_client.dto.OidcPayload;
import brucehan.auth.infrastructure.kakao_client.dto.PublicKeysDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.ZoneId;
import java.util.Base64;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static brucehan.auth.config.exception.ApplicationExceptionType.*;

@Component
public class OidcTokenVerification {
    private static final String RSA = "RSA";

    private final ObjectMapper objectMapper;

    public OidcTokenVerification(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OidcPayload verifyIdToken(String idToken, PublicKeysDto keys) {
        HeaderInfo header = extractFromHeader(idToken);
        PublicKeysDto.JWK matchedKey = keys.getMatchedKey(header.kid(), header.alg());
        PublicKey publicKey = createPublicKey(matchedKey);
        return verifyAndExtractPayload(idToken, publicKey);
    }

    private HeaderInfo extractFromHeader(String token) {
        try {
            String[] chunks = token.split("\\.");
            String headerJson = new String(Base64.getDecoder().decode(chunks[0]), StandardCharsets.UTF_8);
            Map<?, ?> header = objectMapper.readValue(headerJson, Map.class);
            return new HeaderInfo(String.valueOf(header.get("kid")), String.valueOf(header.get("alg")));
        } catch (JsonProcessingException e) {
            throw new ApplicationException(e, JWT_INVALID_FORMAT);
        }
    }

    private PublicKey createPublicKey(PublicKeysDto.JWK matchedKey) {
        byte[] nBytes = Base64.getUrlDecoder().decode(matchedKey.n());
        byte[] eBytes = Base64.getUrlDecoder().decode(matchedKey.e());
        BigInteger modulus = new BigInteger(1, nBytes);
        BigInteger exponent = new BigInteger(1, eBytes);
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, exponent);

        try {
            return KeyFactory.getInstance(RSA).generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new ApplicationException(e, OIDC_PUBLIC_KEY_GENERATION_ERROR);
        }
    }

    private OidcPayload verifyAndExtractPayload(String idToken, PublicKey publicKey) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(idToken)
                .getPayload();
        return new OidcPayload(
                claims.getSubject(),
                claims.getIssuer(),
                claims.get("email", String.class),
                claims.getExpiration()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime(),
                claims.getIssuedAt()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
        );
    }
}
