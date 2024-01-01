package top.zynorl.demo.gatewaydemo.util;

import com.sun.security.auth.UserPrincipal;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.*;

/**
 * jwt 工具类
 */
public final class JwtTokenUtil {
    private static Key getInstance(String salt) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
//        String apiKey = DatatypeConverter.printBase64Binary(salt.getBytes());
//        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(apiKey);
        SecretKeySpec secretKeySpec = new SecretKeySpec(salt.getBytes(), signatureAlgorithm.getJcaName());
        return secretKeySpec;
    }

    /**
     * 生成token
     *
     * @param claims
     * @param salt
     * @param exp
     * @return
     */
    public static String createJavaWebToken4JwtAuth(Map<String, Object> claims, String salt, Integer exp) {
        return Jwts.builder().setClaims(claims).setExpiration(DateUtils.addSeconds(new Date(), exp))
                .signWith(SignatureAlgorithm.HS256, getInstance(salt))
                .compact();
    }

    /**
     * 解构token
     *
     * @param token
     * @param salt
     * @return
     */
    public static Optional<Map<String, Object>> verifyJavaWebToken(String token, String salt) {
        try {
            return Optional.of(Jwts.parser().setSigningKey(getInstance(salt)).parseClaimsJws(token).getBody());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 根据token，获取授权信息
     *
     * @param token
     * @param salt
     * @return
     */
    public static Optional<Authentication> getAuthenticationByToken(String token, String salt) {
        Optional<Map<String, Object>> stringObjectMap = verifyJavaWebToken(token, salt);
        return stringObjectMap.filter(claims -> claims.get("user-info") != null)
                .map(claim -> (LinkedHashMap<String, Object>) claim.get("user-info"))
                .map(claim -> {
                    String username = (String) claim.get("username");
                    List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList((String) claim.get("role"));
                    UserPrincipal userPrincipal = new UserPrincipal(username);
                    return new UsernamePasswordAuthenticationToken(userPrincipal, token, authorities);
                });
    }
    /**
     * 根据token，获取sessionId
     *
     * @param token
     * @param salt
     * @return
     */
    public static Optional<String> getSessionIdByToken(String token, String salt) {
        Optional<Map<String, Object>> stringObjectMap = verifyJavaWebToken(token, salt);
        return stringObjectMap.filter(claims -> claims.get("sessionId") != null)
                .map(claim ->(String)claim.get("sessionId"));
    }
}
