package com.example.uai.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class TokenUtil {

    private static final String SECRET_STRING = "uaiSecretKey1234567890123456789012";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRE_TIME = 60 * 60 * 24; // 24小时，单位秒

    private final CacheUtil cacheUtil;  // 通用缓存

    @Autowired
    public TokenUtil(CacheUtil cacheUtil) {
        this.cacheUtil = cacheUtil;
    }

    // 生成token并存入缓存
    public String generateToken(UUID userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRE_TIME * 1000);

        String token = Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(SECRET_KEY, Jwts.SIG.HS256)
                .compact();

        // 使用CacheUtil存储token
        cacheUtil.set(token, userId.toString(), EXPIRE_TIME);
        return token;
    }

    // 校验token有效性
    public boolean validateToken(String token) {
        try {
            // 验证JWT签名
            Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token);

            // 检查缓存中是否存在
            return cacheUtil.hasKey(token);
        } catch (Exception e) {
            return false;
        }
    }

    // 解析token获取用户id
    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return UUID.fromString(claims.getSubject());
    }

    // 删除token（登出）
    public void deleteToken(String token) {
        cacheUtil.delete(token);
    }
}
