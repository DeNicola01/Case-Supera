package br.com.supera.case_supera.service;

import br.com.supera.case_supera.config.JwtTokenProvider;
import br.com.supera.case_supera.dto.LoginRequest;
import br.com.supera.case_supera.dto.LoginResponse;
import br.com.supera.case_supera.entity.User;
import br.com.supera.case_supera.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            String token = tokenProvider.generateToken(authentication);
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("Usuário não encontrado"));

            return LoginResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .department(user.getDepartment().name())
                    .build();
        } catch (Exception e) {
            throw new BadCredentialsException("Email ou senha inválidos");
        }
    }
}

