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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Join;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
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

    @PersistenceContext
    private EntityManager entityManager;

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

        // Salvar request primeiro para ter ID (necessário para o histórico)
        request = accessRequestRepository.save(request);

        // Validar e processar automaticamente (adiciona histórico se necessário)
        String result = processAutomaticValidation(request, user);

        // Salvar novamente para persistir o histórico adicionado
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

        // Validação de solicitação ativa removida para permitir renovações
        // A validação de acesso já existente (via UserModule) abaixo já cobre esse caso

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
        // Para solicitações novas aprovadas automaticamente, não há status anterior real
        // Usamos ATIVO como status "anterior" apenas para satisfazer a constraint NOT NULL do banco
        addHistory(request, RequestStatus.ATIVO, RequestStatus.ATIVO, "Solicitação aprovada automaticamente");

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

    /**
     * Processa validação automática para renovação de acesso
     * Similar ao processAutomaticValidation, mas não valida acesso já existente
     * (pois estamos renovando acessos que já existem)
     */
    private String processRenewalValidation(AccessRequest request, User user, AccessRequest originalRequest) {
        // Validar compatibilidade de departamento
        for (Module module : request.getRequestedModules()) {
            if (!isDepartmentAllowed(user.getDepartment(), module)) {
                request.setStatus(RequestStatus.NEGADO);
                request.setDenialReason("Departamento sem permissão para acessar este módulo");
                addHistory(request, RequestStatus.ATIVO, RequestStatus.NEGADO, request.getDenialReason());
                return "Renovação negada. Motivo: " + request.getDenialReason();
            }
        }

        // Na renovação, não validamos se há outras solicitações ativas porque:
        // 1. Estamos renovando uma solicitação existente (a original)
        // 2. A nova solicitação de renovação acabou de ser criada e também está ativa
        // Isso é o comportamento esperado para renovações

        // Validar módulos mutuamente exclusivos com outros módulos ativos
        // (excluindo os módulos que estão sendo renovados)
        List<Module> activeModules = userModuleRepository.findActiveModulesByUser(user);
        Set<Module> modulesBeingRenewed = request.getRequestedModules();
        
        // Obter IDs dos módulos da solicitação original para excluir da validação
        Set<Long> originalModuleIds = originalRequest.getRequestedModules().stream()
                .map(Module::getId)
                .collect(Collectors.toSet());
        
        for (Module requestedModule : request.getRequestedModules()) {
            for (Module activeModule : activeModules) {
                // Ignorar se o módulo ativo é um dos que está sendo renovado (da solicitação original)
                if (originalModuleIds.contains(activeModule.getId())) {
                    continue;
                }
                if (areIncompatible(requestedModule, activeModule)) {
                    request.setStatus(RequestStatus.NEGADO);
                    request.setDenialReason("Módulo incompatível com outro módulo já ativo em seu perfil");
                    addHistory(request, RequestStatus.ATIVO, RequestStatus.NEGADO, request.getDenialReason());
                    return "Renovação negada. Motivo: " + request.getDenialReason();
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
                    return "Renovação negada. Motivo: " + request.getDenialReason();
                }
            }
        }

        // Para renovação, não validamos limite de módulos pois estamos apenas estendendo acessos existentes
        // Aprovar renovação
        request.setStatus(RequestStatus.ATIVO);
        addHistory(request, RequestStatus.ATIVO, RequestStatus.ATIVO, "Renovação aprovada automaticamente");

        return "Renovação aprovada";
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
        // Só adiciona histórico se o request já tiver ID (já foi persistido)
        if (request.getId() != null) {
            AccessHistory history = AccessHistory.builder()
                    .accessRequest(request)
                    .previousStatus(previousStatus)
                    .newStatus(newStatus)
                    .changeDate(LocalDateTime.now())
                    .reason(reason)
                    .build();
            // Salva explicitamente já que o request tem ID
            accessHistoryRepository.save(history);
            request.getHistory().add(history);
        } else {
            // Se não tem ID ainda, apenas adiciona à lista (será salvo pelo cascade depois)
            AccessHistory history = AccessHistory.builder()
                    .accessRequest(request)
                    .previousStatus(previousStatus)
                    .newStatus(newStatus)
                    .changeDate(LocalDateTime.now())
                    .reason(reason)
                    .build();
            request.getHistory().add(history);
        }
    }

    public Page<AccessRequestResponseDTO> getUserRequests(Long userId, String searchText, RequestStatus status,
                                                           Boolean urgent, LocalDateTime startDate,
                                                           LocalDateTime endDate, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Usar Criteria API para construir query dinamicamente
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AccessRequest> query = cb.createQuery(AccessRequest.class);
        Root<AccessRequest> root = query.from(AccessRequest.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Filtro obrigatório: usuário
        predicates.add(cb.equal(root.get("user"), user));
        
        // Filtro: searchText (protocolo ou nome do módulo)
        if (searchText != null && !searchText.trim().isEmpty()) {
            String searchPattern = "%" + searchText.trim() + "%";
            Predicate protocolMatch = cb.like(root.get("protocol"), searchPattern);
            
            // Buscar por nome do módulo
            Join<AccessRequest, Module> moduleJoin = root.join("requestedModules");
            Predicate moduleNameMatch = cb.like(moduleJoin.get("name"), searchPattern);
            
            predicates.add(cb.or(protocolMatch, moduleNameMatch));
        }
        
        // Filtro: status
        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }
        
        // Filtro: urgent
        if (urgent != null) {
            predicates.add(cb.equal(root.get("urgent"), urgent));
        }
        
        // Filtro: startDate
        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("requestDate"), startDate));
        }
        
        // Filtro: endDate
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("requestDate"), endDate));
        }
        
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get("requestDate")));
        
        // Contar total (usar DISTINCT para evitar duplicatas por causa do join com módulos)
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<AccessRequest> countRoot = countQuery.from(AccessRequest.class);
        countQuery.select(cb.countDistinct(countRoot));
        
        List<Predicate> countPredicates = new ArrayList<>();
        countPredicates.add(cb.equal(countRoot.get("user"), user));
        
        if (searchText != null && !searchText.trim().isEmpty()) {
            String searchPattern = "%" + searchText.trim() + "%";
            Predicate protocolMatch = cb.like(countRoot.get("protocol"), searchPattern);
            Join<AccessRequest, Module> moduleJoin = countRoot.join("requestedModules");
            Predicate moduleNameMatch = cb.like(moduleJoin.get("name"), searchPattern);
            countPredicates.add(cb.or(protocolMatch, moduleNameMatch));
        }
        
        if (status != null) {
            countPredicates.add(cb.equal(countRoot.get("status"), status));
        }
        
        if (urgent != null) {
            countPredicates.add(cb.equal(countRoot.get("urgent"), urgent));
        }
        
        if (startDate != null) {
            countPredicates.add(cb.greaterThanOrEqualTo(countRoot.get("requestDate"), startDate));
        }
        
        if (endDate != null) {
            countPredicates.add(cb.lessThanOrEqualTo(countRoot.get("requestDate"), endDate));
        }
        
        countQuery.where(countPredicates.toArray(new Predicate[0]));
        
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        
        // Buscar resultados paginados
        TypedQuery<AccessRequest> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<AccessRequest> requests = typedQuery.getResultList();
        
        Page<AccessRequest> page = new PageImpl<>(requests, pageable, total);
        
        return page.map(this::toDTO);
    }

    public AccessRequestResponseDTO getRequestDetails(Long userId, Long requestId) {
        AccessRequest request = accessRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitação não encontrada"));

        if (!request.getUser().getId().equals(userId)) {
            throw new BusinessException("Você não tem permissão para acessar esta solicitação");
        }

        return toDTO(request);
    }

    @Transactional
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

        // Validar que faltam 30 dias ou menos para expiração
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationDate = originalRequest.getExpirationDate();
        
        // Se a data de expiração já passou, permitir renovação
        if (expirationDate.isBefore(now)) {
            // Já expirado, pode renovar
        } else {
            // Calcular dias até expiração (inclusive)
            long daysUntilExpiration = java.time.Duration.between(now, expirationDate).toDays();
            
            // Permitir renovação se faltam 30 dias ou menos (<= 30)
            if (daysUntilExpiration > 30) {
                throw new BusinessException("Renovação só é permitida quando faltam 30 dias ou menos para expiração. Faltam " + daysUntilExpiration + " dias.");
            }
        }

        User user = originalRequest.getUser();
        Set<Module> modulesToRenew = originalRequest.getRequestedModules();

        // Validar módulos ainda ativos
        for (Module module : modulesToRenew) {
            if (!module.getActive()) {
                throw new BusinessException("Módulo não está mais ativo: " + module.getName());
            }
        }

        // Gerar novo protocolo
        String newProtocol = generateProtocol();

        // Criar nova solicitação vinculada à original
        AccessRequest newRequest = AccessRequest.builder()
                .protocol(newProtocol)
                .user(user)
                .requestedModules(new HashSet<>(modulesToRenew))
                .justification("Renovação de acesso - " + originalRequest.getJustification())
                .urgent(originalRequest.getUrgent())
                .requestDate(LocalDateTime.now())
                .renewedFrom(originalRequest)
                .build();

        // Salvar request primeiro para ter ID
        newRequest = accessRequestRepository.save(newRequest);

        // Reaplicar regras de negócio (validações) - versão para renovação
        // Esta validação não verifica se já existe solicitação ativa (pois estamos renovando)
        String validationResult = processRenewalValidation(newRequest, user, originalRequest);

        // Se foi negado, retornar o motivo
        if (newRequest.getStatus() == RequestStatus.NEGADO) {
            accessRequestRepository.save(newRequest);
            return validationResult;
        }

        // Se aprovado, estender validade dos acessos existentes por mais 180 dias
        LocalDateTime newExpirationDate = LocalDateTime.now().plusDays(180);
        newRequest.setExpirationDate(newExpirationDate);
        
        // Atualizar histórico com mensagem mais detalhada sobre a renovação
        if (!newRequest.getHistory().isEmpty()) {
            AccessHistory lastHistory = newRequest.getHistory().get(newRequest.getHistory().size() - 1);
            lastHistory.setReason("Renovação aprovada automaticamente - acesso estendido por 180 dias até " + 
                                     newExpirationDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            accessHistoryRepository.save(lastHistory);
        }

        // Estender validade dos UserModules existentes
        for (Module module : modulesToRenew) {
            userModuleRepository.findByUserAndModuleAndActiveTrue(user, module)
                    .ifPresentOrElse(
                            userModule -> {
                                // Estender validade do acesso existente
                                userModule.setExpirationDate(newExpirationDate);
                                userModuleRepository.save(userModule);
                            },
                            () -> {
                                // Se não existe UserModule ativo, criar novo (caso tenha sido desativado)
                                UserModule newUserModule = UserModule.builder()
                                        .user(user)
                                        .module(module)
                                        .grantedDate(LocalDateTime.now())
                                        .expirationDate(newExpirationDate)
                                        .active(true)
                                        .build();
                                userModuleRepository.save(newUserModule);
                            }
                    );
        }

        // Salvar novamente para persistir o histórico
        accessRequestRepository.save(newRequest);

        return "Renovação realizada com sucesso! Protocolo: " + newProtocol + ". Seus acessos foram estendidos por mais 180 dias até " + newExpirationDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".";
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

