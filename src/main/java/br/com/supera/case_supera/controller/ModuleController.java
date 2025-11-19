package br.com.supera.case_supera.controller;

import br.com.supera.case_supera.dto.ModuleResponseDTO;
import br.com.supera.case_supera.service.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@Tag(name = "Módulos", description = "Endpoints para consultar módulos disponíveis")
@SecurityRequirement(name = "bearerAuth")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @GetMapping
    @Operation(summary = "Listar módulos disponíveis", description = "Retorna todos os módulos disponíveis no sistema")
    public ResponseEntity<List<ModuleResponseDTO>> getAvailableModules() {
        List<ModuleResponseDTO> modules = moduleService.getAvailableModules();
        return ResponseEntity.ok(modules);
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todos os módulos", description = "Retorna todos os módulos (ativos e inativos)")
    public ResponseEntity<List<ModuleResponseDTO>> getAllModules() {
        List<ModuleResponseDTO> modules = moduleService.getAllModules();
        return ResponseEntity.ok(modules);
    }
}

