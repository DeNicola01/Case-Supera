package br.com.supera.case_supera.service;

import br.com.supera.case_supera.config.JwtTokenProvider;
import br.com.supera.case_supera.dto.LoginRequest;
import br.com.supera.case_supera.dto.LoginResponse;
import br.com.supera.case_supera.entity.Department;
import br.com.supera.case_supera.entity.User;
import br.com.supera.case_supera.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@supera.com")
                .password("$2a$10$encrypted")
                .name("Test User")
                .department(Department.TI)
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@supera.com");
        loginRequest.setPassword("senha123");
    }

    @Test
    void testLoginSuccess() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(eq(new UsernamePasswordAuthenticationToken("test@supera.com", "senha123"))))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(eq(authentication))).thenReturn("jwt-token");
        when(userRepository.findByEmail(eq("test@supera.com"))).thenReturn(Optional.of(testUser));

        // Act
        LoginResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("test@supera.com", response.getEmail());
        assertEquals("Test User", response.getName());
        assertEquals("TI", response.getDepartment());

        verify(authenticationManager).authenticate(eq(new UsernamePasswordAuthenticationToken("test@supera.com", "senha123")));
        verify(tokenProvider).generateToken(eq(authentication));
        verify(userRepository).findByEmail(eq("test@supera.com"));
    }

    @Test
    void testLoginInvalidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(eq(new UsernamePasswordAuthenticationToken("test@supera.com", "senha123"))))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));

        verify(authenticationManager).authenticate(eq(new UsernamePasswordAuthenticationToken("test@supera.com", "senha123")));
        verify(tokenProvider, never()).generateToken(isNotNull());
    }

    @Test
    void testLoginUserNotFound() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(eq(new UsernamePasswordAuthenticationToken("test@supera.com", "senha123"))))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(eq(authentication))).thenReturn("jwt-token");
        when(userRepository.findByEmail(eq("test@supera.com"))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));

        verify(userRepository).findByEmail(eq("test@supera.com"));
    }
}

