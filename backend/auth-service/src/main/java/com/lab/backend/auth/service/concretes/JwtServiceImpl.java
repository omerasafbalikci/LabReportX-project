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

@Service
@RequiredArgsConstructor
@Log4j2
public class JwtServiceImpl implements JwtService {
    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    @Value("${jwt.authorities-key}")
    private String AUTHORITIES_KEY;

    @Value("${jwt.access-token-expiration}")
    private long ACCESS_TOKEN_EXPIRATION;

    @Value("${jwt.refresh-token-expiration}")
    private long REFRESH_TOKEN_EXPIRATION;

    @Override
    public String generateAccessToken(String username, List<String> roles) {
        log.info("Generating access token for username: {}", username);
        Claims claims = Jwts.claims().subject(username).add(this.AUTHORITIES_KEY, roles).build();
        String accessToken = buildToken(claims, this.ACCESS_TOKEN_EXPIRATION);
        log.debug("Generated access token: {}", accessToken);
        return accessToken;
    }

    @Override
    public String generateRefreshToken(String username) {
        log.info("Generating refresh token for username: {}", username);
        Claims claims = Jwts.claims().subject(username).build();
        String refreshToken = buildToken(claims, this.REFRESH_TOKEN_EXPIRATION);
        log.debug("Generated refresh token: {}", refreshToken);
        return refreshToken;
    }

    private String buildToken(Claims claims, long expiration) {
        log.trace("Building token with claims: {} and expiration: {}", claims, expiration);
        String token = Jwts.builder()
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
        log.trace("Built token: {}", token);
        return token;
    }

    @Override
    public String extractUsername(String token) {
        log.trace("Extracting username from token.");
        String username = extractClaim(token, Claims::getSubject);
        log.debug("Extracted username: {}", username);
        return username;
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        T claim = claimsResolver.apply(claims);
        log.trace("Extracted claim: {}", claim);
        return claim;
    }

    private Claims extractAllClaims(String token) {
        log.trace("Extracting all claims from token.");
        Claims claims = Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        log.trace("Extracted claims: {}", claims);
        return claims;
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        log.trace("Validating token for username: {}", userDetails.getUsername());
        String username = extractUsername(token);
        boolean isValid = (username.equals(userDetails.getUsername())) && (!isTokenExpired(token));
        log.debug("Token valid: {}", isValid);
        return isValid;
    }

    private boolean isTokenExpired(String token) {
        boolean isExpired = extractExpiration(token).before(new Date());
        log.debug("Token expired: {}", isExpired);
        return isExpired;
    }

    private Date extractExpiration(String token) {
        log.trace("Extracting expiration from token.");
        Date expiration = extractClaim(token, Claims::getExpiration);
        log.debug("Extracted expiration date: {}", expiration);
        return expiration;
    }

    private SecretKey getSignInKey() {
        log.trace("Generating sign-in key.");
        byte[] keyBytes = Decoders.BASE64URL.decode(this.SECRET_KEY);
        SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);
        log.trace("Generated sign-in key.");
        return secretKey;
    }
}
