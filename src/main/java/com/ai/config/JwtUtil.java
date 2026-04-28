package com.ai.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24小时

    /**
     * 生成JWT令牌
     */
    public String generateToken(String userId, String employeeNo, String name) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("employeeNo", employeeNo);
        claims.put("name", name);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 解析JWT令牌
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证令牌是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从令牌中获取用户ID
     */
    public String getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从令牌中获取员工编号
     */
    public String getEmployeeNoFromToken(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("employeeNo");
    }

    /**
     * 从令牌中获取姓名
     */
    public String getNameFromToken(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("name");
    }
}
