package com.qring.reservation.infrastructure.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Base64;

@Component
public class PassportUtil {

    @Value("${service.jwt.secret-key}")
    private String secretKey;

    private static Key key;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    public static Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(removeBearerPrefix(token))
                .getBody();
    }

    private static String removeBearerPrefix(String token) {
        if (StringUtils.hasText(token) && token.startsWith("Bearer")) {
            return token.substring(7);
        }
        return token;
    }

    public static Long getUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }

    public static String getUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public static String getRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public static String getSlackEmail(String token) {
        return extractClaims(token).get("slackEmail", String.class);
    }

}
