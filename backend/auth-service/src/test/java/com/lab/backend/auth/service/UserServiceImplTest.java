package com.lab.backend.auth.service;

import com.lab.backend.auth.dto.requests.AuthRequest;
import com.lab.backend.auth.entity.User;
import com.lab.backend.auth.repository.RoleRepository;
import com.lab.backend.auth.repository.TokenRepository;
import com.lab.backend.auth.repository.UserRepository;
import com.lab.backend.auth.service.abstracts.JwtService;
import com.lab.backend.auth.service.concretes.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import redis.clients.jedis.JedisPool;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private UserDetails userDetails;
    @Mock
    private JedisPool jedisPool;
    @InjectMocks
    private UserServiceImpl userService;
    private AuthRequest authRequest;
    private User user;
}
