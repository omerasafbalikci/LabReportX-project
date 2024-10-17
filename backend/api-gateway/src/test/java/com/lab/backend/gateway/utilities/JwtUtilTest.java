package com.lab.backend.gateway.utilities;

import com.lab.backend.gateway.utilities.exceptions.InvalidTokenException;
import com.lab.backend.gateway.utilities.exceptions.TokenNotFoundException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtUtilTest {
    @Mock
    private JedisPool jedisPool;
    @InjectMocks
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", "6bc18dff62463c49343d083ae0e523fbfb88d4d45b4a9cb62e86d0c53bd9b870");
        ReflectionTestUtils.setField(jwtUtil, "redisHost", "localhost");
        ReflectionTestUtils.setField(jwtUtil, "redisPort", "6379");
    }

    @Test
    void givenValidRedisHostAndPort_whenInitMethodIsCalled_thenJedisPoolInstanceIsInitialized() {
        // Act
        this.jwtUtil.init();
        JedisPool jedisPool = (JedisPool) ReflectionTestUtils.getField(jwtUtil, "jedisPool");

        // Assert
        assertNotNull(jedisPool, "JedisPool instance should be initialized");
    }

    @Test
    void givenInvalidPort_whenInitMethodIsCalled_thenNumberFormatExceptionIsThrown() {
        // Arrange
        ReflectionTestUtils.setField(jwtUtil, "redisPort", "invalidPort");

        // Act & Assert
        assertThrows(NumberFormatException.class, () -> jwtUtil.init(), "Invalid port should throw NumberFormatException");
    }

    @Test
    void testGetClaimsAndValidate_shouldThrowInvalidTokenException_whenTokenIsNull() {
        // Act & Assert
        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () -> jwtUtil.getClaimsAndValidate(null));

        assertEquals("Invalid token", exception.getMessage());
    }

    @Test
    void getClaimsAndValidate_whenTokenIsInvalid_shouldThrowInvalidTokenException() {
        // Arrange
        String token = "invalidToken";

        JwtParserBuilder parserBuilder = mock(JwtParserBuilder.class);
        JwtParser parser = mock(JwtParser.class);
        Mockito.lenient().when(parserBuilder.verifyWith(any(SecretKey.class))).thenReturn(parserBuilder);
        Mockito.lenient().when(parserBuilder.build()).thenReturn(parser);
        Mockito.lenient().when(parser.parseSignedClaims(anyString())).thenThrow(InvalidTokenException.class);

        // Act & Assert
        assertThrows(InvalidTokenException.class, () -> jwtUtil.getClaimsAndValidate(token));
    }

    @Test
    void isLoggedOut_whenTokenIsLoggedOut_shouldReturnTrue() {
        // Arrange
        String token = "loggedOutToken";
        String tokenIdStr = "1";

        Jedis jedis = mock(Jedis.class);
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.get(token)).thenReturn(tokenIdStr);
        when(jedis.get("token:1:is_logged_out")).thenReturn("true");

        // Act
        boolean result = this.jwtUtil.isLoggedOut(token);

        // Assert
        assertTrue(result);

        // Verify
        verify(jedisPool).getResource();
        verify(jedis).get(token);
        verify(jedis).get("token:1:is_logged_out");
    }

    @Test
    void isLoggedOut_whenTokenIsNotLoggedOut_shouldReturnFalse() {
        // Arrange
        String token = "notLoggedOutToken";
        String tokenIdStr = "1";

        Jedis jedis = mock(Jedis.class);
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.get(token)).thenReturn(tokenIdStr);
        when(jedis.get("token:1:is_logged_out")).thenReturn("false");

        // Act
        boolean result = this.jwtUtil.isLoggedOut(token);

        // Assert
        assertFalse(result);

        // Verify
        verify(jedisPool).getResource();
        verify(jedis).get(token);
        verify(jedis).get("token:1:is_logged_out");
    }

    @Test
    void isLoggedOut_whenTokenNotFoundInRedis_shouldThrowTokenNotFoundException() {
        // Arrange
        String token = "nonExistentToken";
        Jedis jedis = mock(Jedis.class);
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.get(anyString())).thenReturn(null);

        // Act & Assert
        assertThrows(TokenNotFoundException.class, () -> jwtUtil.isLoggedOut(token));
    }

    @Test
    void isLoggedOut_whenLogoutStatusNotFoundInRedis_shouldThrowTokenNotFoundException() {
        // Arrange
        String token = "token";
        String tokenIdStr = "1";
        Jedis jedis = mock(Jedis.class);
        when(jedisPool.getResource()).thenReturn(jedis);
        when(jedis.get(token)).thenReturn(tokenIdStr);
        when(jedis.get("token:1:is_logged_out")).thenReturn(null);

        // Act & Assert
        assertThrows(TokenNotFoundException.class, () -> jwtUtil.isLoggedOut(token));
    }

    @Test
    void getRoles_shouldReturnRolesFromClaims() {
        // Arrange
        ReflectionTestUtils.setField(jwtUtil, "AUTHORITIES_KEY", "authorities");
        Claims claims = mock(Claims.class);
        List<String> roles = Collections.singletonList("ROLE_ADMIN");
        when(claims.get("authorities")).thenReturn(roles);

        // Act
        List<String> result = this.jwtUtil.getRoles(claims);

        // Assert
        assertEquals(roles, result);
    }
}
