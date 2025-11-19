package br.com.supera.case_supera.controller;

import br.com.supera.case_supera.dto.*;
import br.com.supera.case_supera.entity.RequestStatus;
import br.com.supera.case_supera.service.AccessRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/requests")
@Tag(name = "Solicitações de Acesso", description = "Endpoints para gerenciar solicitações de acesso a módulos")
@SecurityRequirement(name = "bearerAuth")
public class AccessRequestController {

    private final AccessRequestService accessRequestService;
    private final br.com.supera.case_supera.repository.UserRepository userRepository;

    public AccessRequestController(
            AccessRequestService accessRequestService,
            br.com.supera.case_supera.repository.UserRepository userRepository) {
        this.accessRequestService = accessRequestService;
        this.userRepository = userRepository;
    }

    private Long getCurrentUserId(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"))
                .getId();
    }

    @PostMapping
    @Operation(summary = "Criar solicitação de acesso", description = "Cria uma nova solicitação de acesso a módulos")
    public ResponseEntity<ApiResponse> createRequest(
            @Valid @RequestBody AccessRequestDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        String message = accessRequestService.createAccessRequest(userId, dto);
        return ResponseEntity.ok(ApiResponse.builder().message(message).build());
    }

    @GetMapping
    @Operation(summary = "Listar solicitações", description = "Lista as solicitações do usuário com filtros e paginação")
    public ResponseEntity<Page<AccessRequestResponseDTO>> getRequests(
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) Boolean urgent,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<AccessRequestResponseDTO> requests = accessRequestService.getUserRequests(
                userId, searchText, status, urgent, startDate, endDate, pageable);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhes da solicitação", description = "Retorna os detalhes completos de uma solicitação")
    public ResponseEntity<AccessRequestResponseDTO> getRequestDetails(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        AccessRequestResponseDTO request = accessRequestService.getRequestDetails(userId, id);
        return ResponseEntity.ok(request);
    }

    @PostMapping("/{id}/renew")
    @Operation(summary = "Renovar acesso", description = "Renova o acesso a módulos quando faltam menos de 30 dias")
    public ResponseEntity<ApiResponse> renewAccess(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        String message = accessRequestService.renewAccess(userId, id);
        return ResponseEntity.ok(ApiResponse.builder().message(message).build());
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar solicitação", description = "Cancela uma solicitação ativa")
    public ResponseEntity<ApiResponse> cancelRequest(
            @PathVariable Long id,
            @Valid @RequestBody CancelRequestDTO dto,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        accessRequestService.cancelRequest(userId, id, dto.getReason());
        return ResponseEntity.ok(ApiResponse.builder().message("Solicitação cancelada com sucesso").build());
    }
}

