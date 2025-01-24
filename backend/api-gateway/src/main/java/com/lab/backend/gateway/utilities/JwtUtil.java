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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for handling JWT token operations such as validation, role extraction, and checking if a token
 * is logged out. This class also integrates Redis for token management.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Component
@Log4j2
public class JwtUtil {
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.authorities-key}")
    private String authoritiesKey;

    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private String redisPort;

    private JedisPool jedisPool;

    /**
     * Initializes the Jedis connection pool used for Redis operations.
     */
    @PostConstruct
    public void init() {
        log.trace("Initializing Redis connection pool with host: {}, port: {}", redisHost, redisPort);
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        this.jedisPool = new JedisPool(poolConfig, redisHost, Integer.parseInt(redisPort));
        log.trace("Redis connection pool initialized successfully.");
    }

    /**
     * Validates the provided JWT token and returns the associated claims.
     *
     * @param token the JWT token to validate.
     * @return the claims extracted from the token if valid.
     * @throws InvalidTokenException if the token is invalid.
     */
    public Claims getClaimsAndValidate(String token) {
        log.trace("Entering getClaimsAndValidate method in JwtUtils");
        log.debug("Validating token: {}", token);
        try {
            Claims claims = Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
            log.info("Token validated successfully. Claims: {}", claims);
            return claims;
        } catch (JwtException | IllegalArgumentException exception) {
            log.error("Error while parsing token: {}", exception.getMessage());
            throw new InvalidTokenException("Invalid token");
        } finally {
            log.trace("Exiting getClaimsAndValidate method in JwtUtils");
        }
    }

    /**
     * Checks if the provided token has been logged out using Redis.
     *
     * @param token the JWT token to check.
     * @return true if the token is logged out, false otherwise.
     * @throws TokenNotFoundException if the token or its status is not found in Redis.
     */
    public boolean isLoggedOut(String token) {
        log.trace("Entering isLoggedOut method in JwtUtils");
        log.debug("Checking if token is logged out: {}", token);
        try (Jedis jedis = this.jedisPool.getResource()) {
            String tokenIdStr = jedis.get(token);
            if (tokenIdStr == null) {
                log.error("Token not found in Redis: {}", token);
                throw new TokenNotFoundException("Token not found in Redis");
            }

            long tokenId = Long.parseLong(tokenIdStr);
            String key = "token:" + tokenId + ":is_logged_out";
            String value = jedis.get(key);
            if (value == null) {
                log.error("Logout status information not found for token: {}", token);
                throw new TokenNotFoundException("Token's logout status information not found in Redis");
            }
            boolean isLoggedOut = Boolean.parseBoolean(value);
            log.info("Token {} logout status: {}", token, isLoggedOut);
            return isLoggedOut;
        } finally {
            log.trace("Exiting isLoggedOut method in JwtUtils");
        }
    }

    /**
     * Extracts roles from the provided claims.
     *
     * @param claims the claims extracted from the JWT token.
     * @return a list of roles associated with the claims.
     */
    public List<String> getRoles(Claims claims) {
        log.trace("Entering getRoles method in JwtUtils");
        log.debug("Extracting roles from claims: {}", claims);
        Object rolesObject = claims.get(this.authoritiesKey);
        if (rolesObject instanceof List<?>) {
            List<String> roles = new ArrayList<>();
            for (Object role : (List<?>) rolesObject) {
                if (role instanceof String) {
                    roles.add((String) role);
                }
            }
            log.info("Roles extracted: {}", roles);
            log.trace("Exiting getRoles method in JwtUtils");
            return roles;
        }
        log.warn("No roles found in claims or roles are not of the expected type.");
        return Collections.emptyList();
    }

    /**
     * Retrieves the secret key used for signing the JWT token.
     *
     * @return the secret key.
     */
    private SecretKey getSignInKey() {
        log.trace("Entering getSignInKey method in JwtUtils");
        log.debug("Retrieving secret key for JWT validation.");
        byte[] keyBytes = Decoders.BASE64URL.decode(this.secretKey);
        SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);
        log.debug("Secret key retrieved successfully.");
        log.trace("Exiting getSignInKey method in JwtUtils");
        return secretKey;
    }
}
