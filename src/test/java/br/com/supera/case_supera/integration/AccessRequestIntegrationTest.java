package br.com.supera.case_supera.integration;

import br.com.supera.case_supera.dto.AccessRequestDTO;
import br.com.supera.case_supera.entity.Department;
import br.com.supera.case_supera.entity.Module;
import br.com.supera.case_supera.entity.User;
import br.com.supera.case_supera.repository.ModuleRepository;
import br.com.supera.case_supera.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AccessRequestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Module testModule;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@supera.com")
                .password(passwordEncoder.encode("senha123"))
                .name("Test User")
                .department(Department.TI)
                .build();
        testUser = userRepository.save(testUser);

        // Verificar se o módulo já existe (criado pelo DataInitializer)
        testModule = moduleRepository.findByName("Portal do Colaborador")
                .orElseGet(() -> {
                    Module newModule = Module.builder()
                            .name("Portal do Colaborador")
                            .description("Portal principal")
                            .active(true)
                            .allowedDepartments(new HashSet<>(Arrays.asList(Department.TI)))
                            .incompatibleModules(new HashSet<>())
                            .build();
                    return moduleRepository.save(newModule);
                });
    }

    @Test
    void testCreateAccessRequest() throws Exception {
        // Primeiro fazer login para obter token
        String loginJson = "{\"email\":\"test@supera.com\",\"password\":\"senha123\"}";
        String tokenResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        // Extrair token (simplificado para teste)
        String token = extractTokenFromResponse(tokenResponse);
        
        AccessRequestDTO dto = new AccessRequestDTO();
        dto.setModuleIds(Arrays.asList(testModule.getId()));
        dto.setJustification("Preciso deste módulo para realizar minhas atividades profissionais diárias");
        dto.setUrgent(false);

        mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testGetUserRequests() throws Exception {
        // Primeiro fazer login para obter token
        String loginJson = "{\"email\":\"test@supera.com\",\"password\":\"senha123\"}";
        String tokenResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        String token = extractTokenFromResponse(tokenResponse);
        
        mockMvc.perform(get("/api/requests")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
    
    private String extractTokenFromResponse(String response) {
        // Extrai o token do JSON de resposta
        int start = response.indexOf("\"token\":\"") + 9;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }
}

