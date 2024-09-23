package com.lab.backend.gateway.utilities;

import com.lab.backend.gateway.utilities.exceptions.InvalidTokenException;
import com.lab.backend.gateway.utilities.exceptions.TokenNotFoundException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Log4j2
public class JwtUtil {
    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    @Value("${jwt.authorities-key}")
    private String AUTHORITIES_KEY;

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private String redisPort;

    private JedisPool jedisPool;

    @PostConstruct
    public void init() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        this.jedisPool = new JedisPool(poolConfig, redisHost, Integer.parseInt(redisPort));
    }

    public Claims getClaimsAndValidate(String token) {
        try {
            return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException exception) {
            throw new InvalidTokenException("Invalid token");
        } finally {

        }
    }

    public boolean isLoggedOut(String token) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            String tokenIdStr = jedis.get(token);
            if (tokenIdStr == null) {
                throw new TokenNotFoundException("Token not found in Redis");
            }

            long tokenId = Long.parseLong(tokenIdStr);
            String key = "token:" + tokenId + ":is_logged_out";

            String value = jedis.get(key);
            if (value == null) {
                throw new TokenNotFoundException("Token's logout status information not found in Redis");
            }
            return Boolean.parseBoolean(value);
        } finally {

        }
    }

    public List<String> getRoles(Claims claims) {
        return (List<String>) claims.get(this.AUTHORITIES_KEY);

    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(this.SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
