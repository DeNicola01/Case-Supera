package br.com.supera.case_supera.controller;

import br.com.supera.case_supera.dto.ModuleResponseDTO;
import br.com.supera.case_supera.service.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import br.com.supera.case_supera.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@Tag(name = "Módulos", description = "Endpoints para consultar módulos disponíveis")
@SecurityRequirement(name = "bearerAuth")
public class ModuleController {

    private final ModuleService moduleService;
    private final UserRepository userRepository;

    public ModuleController(ModuleService moduleService, UserRepository userRepository) {
        this.moduleService = moduleService;
        this.userRepository = userRepository;
    }

    private br.com.supera.case_supera.entity.User getCurrentUser(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    @GetMapping
    @Operation(summary = "Listar módulos disponíveis", description = "Retorna os módulos disponíveis para o departamento do usuário autenticado")
    public ResponseEntity<List<ModuleResponseDTO>> getAvailableModules(Authentication authentication) {
        br.com.supera.case_supera.entity.User user = getCurrentUser(authentication);
        List<ModuleResponseDTO> modules = moduleService.getAvailableModules(user);
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todos os módulos", description = "Retorna todos os módulos (ativos e inativos)")
    public ResponseEntity<List<ModuleResponseDTO>> getAllModules() {
        List<ModuleResponseDTO> modules = moduleService.getAllModules();
        return ResponseEntity.ok(modules);
    }
}

