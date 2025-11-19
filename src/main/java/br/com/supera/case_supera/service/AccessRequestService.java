package br.com.supera.case_supera.service;

import br.com.supera.case_supera.dto.AccessRequestDTO;
import br.com.supera.case_supera.dto.AccessRequestResponseDTO;
import br.com.supera.case_supera.dto.AccessHistoryDTO;
import br.com.supera.case_supera.entity.AccessHistory;
import br.com.supera.case_supera.entity.AccessRequest;
import br.com.supera.case_supera.entity.Department;
import br.com.supera.case_supera.entity.Module;
import br.com.supera.case_supera.entity.RequestStatus;
import br.com.supera.case_supera.entity.User;
import br.com.supera.case_supera.entity.UserModule;
import br.com.supera.case_supera.exception.BusinessException;
import br.com.supera.case_supera.exception.ResourceNotFoundException;
import br.com.supera.case_supera.repository.AccessHistoryRepository;
import br.com.supera.case_supera.repository.AccessRequestRepository;
import br.com.supera.case_supera.repository.ModuleRepository;
import br.com.supera.case_supera.repository.UserModuleRepository;
import br.com.supera.case_supera.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccessRequestService {

    private final AccessRequestRepository accessRequestRepository;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;
    private final UserModuleRepository userModuleRepository;
    private final AccessHistoryRepository accessHistoryRepository;

    public AccessRequestService(
            AccessRequestRepository accessRequestRepository,
            UserRepository userRepository,
            ModuleRepository moduleRepository,
            UserModuleRepository userModuleRepository,
            AccessHistoryRepository accessHistoryRepository) {
        this.accessRequestRepository = accessRequestRepository;
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
        this.userModuleRepository = userModuleRepository;
        this.accessHistoryRepository = accessHistoryRepository;
    }

    public String createAccessRequest(Long userId, AccessRequestDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Validar módulos
        Set<Module> requestedModules = dto.getModuleIds().stream()
                .map(id -> moduleRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Módulo não encontrado: " + id)))
                .collect(Collectors.toSet());

        // Validações de negócio
        validateRequest(user, requestedModules, dto.getJustification());

        // Gerar protocolo
        String protocol = generateProtocol();

        // Criar solicitação
        AccessRequest request = AccessRequest.builder()
                .protocol(protocol)
                .user(user)
                .requestedModules(requestedModules)
                .justification(dto.getJustification())
                .urgent(dto.getUrgent() != null && dto.getUrgent())
                .requestDate(LocalDateTime.now())
                .build();

        // Validar e processar automaticamente
        String result = processAutomaticValidation(request, user);

        accessRequestRepository.save(request);
        return result;
    }

    private void validateRequest(User user, Set<Module> requestedModules, String justification) {
        // Validar módulos ativos
        for (Module module : requestedModules) {
            if (!module.getActive()) {
                throw new BusinessException("Módulo não está ativo: " + module.getName());
            }
        }

        // Validar solicitação ativa para mesmo módulo
        for (Module module : requestedModules) {
            List<AccessRequest> activeRequests = accessRequestRepository
                    .findActiveRequestsByUserAndModule(user, module);
            if (!activeRequests.isEmpty()) {
                throw new BusinessException("Você já possui uma solicitação ativa para o módulo: " + module.getName());
            }
        }

        // Validar acesso já existente
        List<Module> activeModules = userModuleRepository.findActiveModulesByUser(user);
        for (Module module : requestedModules) {
            if (activeModules.contains(module)) {
                throw new BusinessException("Você já possui acesso ativo ao módulo: " + module.getName());
            }
        }

        // Validar justificativa genérica
        if (isGenericJustification(justification)) {
            throw new BusinessException("Justificativa insuficiente ou genérica");
        }
    }

    private boolean isGenericJustification(String justification) {
        String lower = justification.toLowerCase().trim();
        return lower.equals("teste") || 
               lower.equals("aaa") || 
               lower.equals("preciso") ||
               lower.length() < 20 ||
               lower.matches("^[a-z\\s]{1,20}$");
    }

    private String processAutomaticValidation(AccessRequest request, User user) {
        // Validar compatibilidade de departamento
        for (Module module : request.getRequestedModules()) {
            if (!isDepartmentAllowed(user.getDepartment(), module)) {
                request.setStatus(RequestStatus.NEGADO);
                request.setDenialReason("Departamento sem permissão para acessar este módulo");
                addHistory(request, RequestStatus.ATIVO, RequestStatus.NEGADO, request.getDenialReason());
                return "Solicitação negada. Motivo: " + request.getDenialReason();
            }
        }

        // Validar módulos mutuamente exclusivos
        List<Module> activeModules = userModuleRepository.findActiveModulesByUser(user);
        for (Module requestedModule : request.getRequestedModules()) {
            for (Module activeModule : activeModules) {
                if (areIncompatible(requestedModule, activeModule)) {
                    request.setStatus(RequestStatus.NEGADO);
                    request.setDenialReason("Módulo incompatível com outro módulo já ativo em seu perfil");
                    addHistory(request, RequestStatus.ATIVO, RequestStatus.NEGADO, request.getDenialReason());
                    return "Solicitação negada. Motivo: " + request.getDenialReason();
                }
            }
        }

        // Validar módulos mutuamente exclusivos entre os solicitados
        List<Module> requestedList = List.copyOf(request.getRequestedModules());
        for (int i = 0; i < requestedList.size(); i++) {
            for (int j = i + 1; j < requestedList.size(); j++) {
                if (areIncompatible(requestedList.get(i), requestedList.get(j))) {
                    request.setStatus(RequestStatus.NEGADO);
                    request.setDenialReason("Módulo incompatível com outro módulo já ativo em seu perfil");
                    addHistory(request, RequestStatus.ATIVO, RequestStatus.NEGADO, request.getDenialReason());
                    return "Solicitação negada. Motivo: " + request.getDenialReason();
                }
            }
        }

        // Validar limite de módulos
        long activeCount = userModuleRepository.countActiveModulesByUser(user);
        int maxModules = user.getDepartment() == Department.TI ? 10 : 5;
        
        if (activeCount + request.getRequestedModules().size() > maxModules) {
            request.setStatus(RequestStatus.NEGADO);
            request.setDenialReason("Limite de módulos ativos atingido");
            addHistory(request, RequestStatus.ATIVO, RequestStatus.NEGADO, request.getDenialReason());
            return "Solicitação negada. Motivo: " + request.getDenialReason();
        }

        // Aprovar e conceder acesso
        request.setStatus(RequestStatus.ATIVO);
        request.setExpirationDate(LocalDateTime.now().plusDays(180));
        addHistory(request, null, RequestStatus.ATIVO, "Solicitação aprovada automaticamente");

        // Conceder acesso aos módulos
        for (Module module : request.getRequestedModules()) {
            UserModule userModule = UserModule.builder()
                    .user(user)
                    .module(module)
                    .grantedDate(LocalDateTime.now())
                    .expirationDate(LocalDateTime.now().plusDays(180))
                    .active(true)
                    .build();
            userModuleRepository.save(userModule);
        }

        return "Solicitação criada com sucesso! Protocolo: " + request.getProtocol() + ". Seus acessos já estão disponíveis!";
    }

    private boolean isDepartmentAllowed(Department department, Module module) {
        if (department == Department.TI) {
            return true; // TI pode acessar todos os módulos
        }

        Set<Department> allowed = module.getAllowedDepartments();
        if (allowed.isEmpty()) {
            // Se não especificado, apenas Portal e Relatórios
            return module.getName().equals("Portal do Colaborador") || 
                   module.getName().equals("Relatórios Gerenciais");
        }

        return allowed.contains(department);
    }

    private boolean areIncompatible(Module module1, Module module2) {
        return module1.getIncompatibleModules().contains(module2) ||
               module2.getIncompatibleModules().contains(module1);
    }

    private String generateProtocol() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = accessRequestRepository.count();
        String sequence = String.format("%04d", count + 1);
        return "SOL-" + datePart + "-" + sequence;
    }

    private void addHistory(AccessRequest request, RequestStatus previousStatus, RequestStatus newStatus, String reason) {
        AccessHistory history = AccessHistory.builder()
                .accessRequest(request)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .changeDate(LocalDateTime.now())
                .reason(reason)
                .build();
        accessHistoryRepository.save(history);
        request.getHistory().add(history);
    }

    public Page<AccessRequestResponseDTO> getUserRequests(Long userId, String searchText, RequestStatus status,
                                                           Boolean urgent, LocalDateTime startDate,
                                                           LocalDateTime endDate, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Page<AccessRequest> requests = accessRequestRepository.findByUserWithFilters(
                user, searchText, status, urgent, startDate, endDate, pageable);

        return requests.map(this::toDTO);
    }

    public AccessRequestResponseDTO getRequestDetails(Long userId, Long requestId) {
        AccessRequest request = accessRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));

        if (!request.getUser().getId().equals(userId)) {
            throw new BusinessException("Você não tem permissão para acessar esta solicitação");
        }

        return toDTO(request);
    }

    public String renewAccess(Long userId, Long requestId) {
        AccessRequest originalRequest = accessRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));

        if (!originalRequest.getUser().getId().equals(userId)) {
            throw new BusinessException("Você não tem permissão para renovar esta solicitação");
        }

        if (originalRequest.getStatus() != RequestStatus.ATIVO) {
            throw new BusinessException("Apenas solicitações ativas podem ser renovadas");
        }

        if (originalRequest.getExpirationDate() == null) {
            throw new BusinessException("Solicitação não possui data de expiração");
        }

        LocalDateTime now = LocalDateTime.now();
        if (originalRequest.getExpirationDate().isAfter(now.plusDays(30))) {
            throw new BusinessException("Renovação só é permitida quando faltam menos de 30 dias para expiração");
        }

        // Criar nova solicitação vinculada
        AccessRequestDTO newRequestDTO = new AccessRequestDTO();
        newRequestDTO.setModuleIds(originalRequest.getRequestedModules().stream()
                .map(Module::getId)
                .collect(Collectors.toList()));
        newRequestDTO.setJustification("Renovação de acesso - " + originalRequest.getJustification());
        newRequestDTO.setUrgent(originalRequest.getUrgent());

        String result = createAccessRequest(userId, newRequestDTO);
        
        // Extrair protocolo do resultado para vincular à solicitação original
        // Formato esperado: "Solicitação criada com sucesso! Protocolo: SOL-20240101-0001. Seus acessos já estão disponíveis!"
        String protocolStart = "Protocolo: ";
        int startIdx = result.indexOf(protocolStart);
        if (startIdx >= 0) {
            startIdx += protocolStart.length();
            int endIdx = result.indexOf(".", startIdx);
            if (endIdx > startIdx) {
                String newProtocol = result.substring(startIdx, endIdx).trim();
                AccessRequest newRequest = accessRequestRepository.findByProtocol(newProtocol)
                        .orElse(null);
                if (newRequest != null) {
                    newRequest.setRenewedFrom(originalRequest);
                    accessRequestRepository.save(newRequest);
                }
            }
        }

        return result;
    }

    public void cancelRequest(Long userId, Long requestId, String reason) {
        AccessRequest request = accessRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));

        if (!request.getUser().getId().equals(userId)) {
            throw new BusinessException("Você não tem permissão para cancelar esta solicitação");
        }

        if (request.getStatus() != RequestStatus.ATIVO) {
            throw new BusinessException("Apenas solicitações ativas podem ser canceladas");
        }

        request.setStatus(RequestStatus.CANCELADO);
        addHistory(request, RequestStatus.ATIVO, RequestStatus.CANCELADO, reason);

        // Revogar acessos
        List<UserModule> userModules = userModuleRepository.findByUserAndActiveTrue(request.getUser());
        for (UserModule userModule : userModules) {
            if (request.getRequestedModules().contains(userModule.getModule())) {
                userModule.setActive(false);
                userModuleRepository.save(userModule);
            }
        }

        accessRequestRepository.save(request);
    }

    private AccessRequestResponseDTO toDTO(AccessRequest request) {
        return AccessRequestResponseDTO.builder()
                .id(request.getId())
                .protocol(request.getProtocol())
                .requestedModules(request.getRequestedModules().stream()
                        .map(Module::getName)
                        .collect(Collectors.toList()))
                .justification(request.getJustification())
                .urgent(request.getUrgent())
                .status(request.getStatus())
                .requestDate(request.getRequestDate())
                .expirationDate(request.getExpirationDate())
                .denialReason(request.getDenialReason())
                .history(request.getHistory().stream()
                        .map(h -> AccessHistoryDTO.builder()
                                .previousStatus(h.getPreviousStatus())
                                .newStatus(h.getNewStatus())
                                .changeDate(h.getChangeDate())
                                .reason(h.getReason())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}

