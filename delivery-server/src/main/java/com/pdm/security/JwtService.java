package com.pdm.security;

import com.pdm.common.BizException;
import com.pdm.common.ErrorCode;
import com.pdm.common.PdmProperties;
import com.pdm.service.ConfigService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final PdmProperties props;
    private final ConfigService configService;

    private SecretKey key() {
        byte[] raw = props.getJwtSecret().getBytes(StandardCharsets.UTF_8);
        if (raw.length < 32) {
            raw = Arrays.copyOf(raw, 32);
        }
        return Keys.hmacShaKeyFor(raw);
    }

    public String create(Long userId, String username, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expireHours() * 3600_000L);
        return Jwts.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key())
                .compact();
    }

    public Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "登录已失效，请重新登录");
        }
    }

    /** 有效期优先取系统参数 login.tokenExpireHours，未配置或非法时回退到 PDM_JWT_EXPIRE_HOURS */
    public long expireHours() {
        int h = configService.getInt("login.tokenExpireHours", 0);
        return h > 0 ? h : props.getJwtExpireHours();
    }
}