package brucehan.auth.application;

import brucehan.auth.infrastructure.kakao_client.dto.OIDCPublicKey;
import brucehan.auth.infrastructure.kakao_client.dto.OIDCPublicKeyList;
import brucehan.auth.infrastructure.kakao_client.dto.OidcPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@Component
public class OIDCTokenVerification {
    private final ObjectMapper objectMapper;

    public OIDCTokenVerification(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // id 토큰을 식별하기 위함.
    public OidcPayload verifyIdToken(
            String idToken,
            OIDCPublicKeyList oidcPublicKeys
    ) {
        HeaderInfo headerInfo = extractFromHeader(idToken); // id token에서 kid, alg 추출
        OIDCPublicKey matchedKey = oidcPublicKeys.getMatchedKey(headerInfo.kid(), headerInfo.alg()); // 추출한 두 값을
        PublicKey publicKey = createPublicKey(matchedKey); // oidc 공개키로 public key 생성
        return verifyAndExtractPayload(idToken, publicKey);
    }

    // 토큰 헤더에서 kid 및 alg 추출
    private HeaderInfo extractFromHeader(String token) {
        try {
            // JWT스펙(RFC 7519)에서는 Base64URL을 강제
            String headerJson = new String(Base64.getUrlDecoder().decode(token.split("\\.")[0])); // .으로 나눈 뒤 제일 첫번째 값(헤더)을 가져
            Map<String, ?> header = objectMapper.readValue(headerJson, Map.class); // Map으로 역직렬화
            return new HeaderInfo( // 값을 가져와서 객체 생성
                    String.valueOf(header.get("kid")),
                    String.valueOf(header.get("alg"))
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // RSA 공개키 생성
    private PublicKey createPublicKey(OIDCPublicKey publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // RSA 암호화 알고리즘으로 인스턴스 생성
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec( // 갖고온 OIDC 공개키로 디코딩해서 RSA 공개키 스펙 생성
                    new BigInteger(1, Base64.getUrlDecoder().decode(publicKey.n())),
                    new BigInteger(1, Base64.getUrlDecoder().decode(publicKey.e()))
            );
            return keyFactory.generatePublic(keySpec); // 스펙 생성한 걸로 RSA 공개키 생성
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    // ID Token 검증 및 페이로드 추출
    private OidcPayload verifyAndExtractPayload(String token, PublicKey publicKey) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey) // "파싱할 모든 서명된 JWT의 서명을 단 하나의 공개키로 검증하겠다"
                .requireIssuer("https://kauth.kakao.com")
                .requireAudience("35c95f246e0eb4e1e284f5e7fb74263e") // 새걸로 갈아끼울 예정
                .requireExpiration(new Date())
                .build()
                .parseSignedClaims(token) // "이 문자열은 서명된(JWS), Claims 페이로드를 가진 JWT일 것이다"
                .getPayload();

        String nonce = String.valueOf(claims.get("nonce"));
        if (!nonce.equals("brucehan")) {
            throw new RuntimeException("요청 토큰이 잘못되었습니다.");
        }


        return new OidcPayload(
                claims.getSubject(),
                String.valueOf(claims.get("email")),
                String.valueOf(claims.get("picture")),
                String.valueOf(claims.get("nickname"))
        );
    }

    record HeaderInfo(
            String kid,
            String alg
    ) {

    }
    /**
     * iss : "내가 신뢰하는 발급자(카카오)가 발급한 토큰인가" 확인. 안 하면 다른 OIDC provider의 토큰도 통과됨
     * aud : "내 앱으로부터 발급된 토큰인가" 확인. 안 하면 같은 카카오라도 다른 앱의 토큰이 통과됨
     * exp : 만료 체크. 이게 빠지면 만료된 토큰도 통과되는 가장 큰 구멍
     * nonce : "내가 인가 요청 시 만든 nonce랑 같은가" 확인. 안 하면 누가 가로챈 ID Token을 재사용하는 공격이 가능
     */
}
