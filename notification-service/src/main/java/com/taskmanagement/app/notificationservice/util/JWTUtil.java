package com.taskmanagement.app.notificationservice.util;
import io.jsonwebtoken.Claims; import io.jsonwebtoken.Jwts; import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys; import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey; import java.security.Key; import java.util.Date; import java.util.function.Function;
@Component
public class JWTUtil {
    @Value("${jwt.secret}")
    private String secretKey;
    public Key getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
    public String extractUsername(String t) {
        return extractClaim(t, Claims::getSubject);
    }
    public String extractRole(String t) {
        return extractClaim(t, c -> c.get("role", String.class));
    }
    public Long extractUserId(String t) {
        return extractClaim(t, c -> { Object u = c.get("userId"); return u instanceof Number ? ((Number)u).longValue() : null; });
    }
    public <T> T extractClaim(String t, Function<Claims,T> r) {
        return r.apply(Jwts.parser().verifyWith((SecretKey)getKey()).build().parseSignedClaims(t).getPayload());
    }
    public boolean validateToken(String t) {
        try {
            return !extractClaim(t,Claims::getExpiration).before(new Date());
        } catch(Exception e) {
            return false;
        }
    }
}
