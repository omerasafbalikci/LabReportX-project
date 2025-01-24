package com.lab.backend.auth.service.concretes;

import com.lab.backend.auth.service.abstracts.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * Implementation of {@link JwtService} that handles JWT creation, validation, and claim extraction.
 *
 * @author Ömer Asaf BALIKÇI
 */

@Service
@RequiredArgsConstructor
@Log4j2
public class JwtServiceImpl implements JwtService {
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.authorities-key}")
    private String authoritiesKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * Generates an access token for the specified username and roles.
     *
     * @param username the username for which the token is generated
     * @param roles    the roles assigned to the user
     * @return the generated access token
     */
    @Override
    public String generateAccessToken(String username, List<String> roles) {
        log.trace("Entering generateAccessToken method in JwtUtils class with username: {}", username);
        Claims claims = Jwts.claims().subject(username).add(this.authoritiesKey, roles).build();
        String accessToken = buildToken(claims, this.accessTokenExpiration);
        log.trace("Exiting generateAccessToken method in JwtUtils class with accessToken: {}", accessToken);
        return accessToken;
    }

    /**
     * Generates a refresh token for the specified username.
     *
     * @param username the username for which the refresh token is generated
     * @return the generated refresh token
     */
    @Override
    public String generateRefreshToken(String username) {
        log.trace("Entering generateRefreshToken method in JwtUtils class with username: {}", username);
        Claims claims = Jwts.claims().subject(username).build();
        String refreshToken = buildToken(claims, this.refreshTokenExpiration);
        log.trace("Exiting generateRefreshToken method in JwtUtils class with refreshToken: {}", refreshToken);
        return refreshToken;
    }

    /**
     * Builds a JWT token using the provided claims and expiration time.
     *
     * @param claims     the claims to include in the token
     * @param expiration the token expiration time in milliseconds
     * @return the generated token
     */
    public String buildToken(Claims claims, long expiration) {
        log.trace("Entering buildToken method in JwtUtils class with claims and expiration: {}", expiration);
        String token = Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
        log.trace("Exiting buildToken method in JwtUtils class with token: {}", token);
        return token;
    }

    /**
     * Extracts the username from a JWT token.
     *
     * @param token the JWT token
     * @return the extracted username
     */
    @Override
    public String extractUsername(String token) {
        log.trace("Entering extractUsername method in JwtUtils class");
        String username = extractClaim(token, Claims::getSubject);
        log.trace("Exiting extractUsername method in JwtUtils class with username: {}", username);
        return username;
    }

    /**
     * Extracts a specific claim from the token using the provided claim resolver.
     *
     * @param token          the JWT token
     * @param claimsResolver the function to resolve the claim
     * @param <T>            the type of the claim
     * @return the extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.trace("Entering extractClaim method in JwtUtils class");
        Claims claims = extractAllClaims(token);
        T claim = claimsResolver.apply(claims);
        log.trace("Exiting extractClaim method in JwtUtils class with claim: {}", claim);
        return claim;
    }

    /**
     * Extracts all claims from the JWT token.
     *
     * @param token the JWT token
     * @return the extracted claims
     */
    private Claims extractAllClaims(String token) {
        log.trace("Entering extractAllClaims method in JwtUtils class");
        Claims claims = Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        log.trace("Exiting extractAllClaims method in JwtUtils class");
        return claims;
    }

    /**
     * Validates the JWT token for the given user details.
     *
     * @param token       the JWT token to validate
     * @param userDetails the user details to validate the token against
     * @return true if the token is valid, false otherwise
     */
    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        log.trace("Entering isTokenValid method in JwtUtils class with token and user: {}", userDetails.getUsername());
        String username = extractUsername(token);
        boolean isValid = (username.equals(userDetails.getUsername())) && (!isTokenExpired(token));
        log.trace("Exiting isTokenValid method in JwtUtils class with result: {}", isValid);
        return isValid;
    }

    /**
     * Checks if the token has expired.
     *
     * @param token the JWT token
     * @return true if the token has expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        log.trace("Entering isTokenExpired method in JwtUtils class");
        boolean isExpired = extractExpiration(token).before(new Date());
        log.trace("Exiting isTokenExpired method in JwtUtils class with result: {}", isExpired);
        return isExpired;
    }

    /**
     * Extracts the expiration date from the JWT token.
     *
     * @param token the JWT token
     * @return the expiration date
     */
    private Date extractExpiration(String token) {
        log.trace("Entering extractExpiration method in JwtUtils class");
        Date expiration = extractClaim(token, Claims::getExpiration);
        log.trace("Exiting extractExpiration method in JwtUtils class with expiration date: {}", expiration);
        return expiration;
    }

    /**
     * Retrieves the signing key for JWT using the secret key.
     *
     * @return the {@link SecretKey} used for signing the JWT
     */
    private SecretKey getSignInKey() {
        log.trace("Entering getSignInKey method in JwtUtils class");
        byte[] keyBytes = Decoders.BASE64URL.decode(this.secretKey);
        SecretKey sKey = Keys.hmacShaKeyFor(keyBytes);
        log.trace("Exiting getSignInKey method in JwtUtils class with key");
        return sKey;
    }
}
