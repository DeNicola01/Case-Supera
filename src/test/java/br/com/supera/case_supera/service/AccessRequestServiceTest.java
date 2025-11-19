package br.com.supera.case_supera.service;

import br.com.supera.case_supera.dto.AccessRequestDTO;
import br.com.supera.case_supera.dto.AccessRequestResponseDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessRequestServiceTest {

    @Mock
    private AccessRequestRepository accessRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private UserModuleRepository userModuleRepository;

    @Mock
    private AccessHistoryRepository accessHistoryRepository;

    @InjectMocks
    private AccessRequestService accessRequestService;

    private User testUser;
    private Module testModule1;
    private Module testModule2;
    private AccessRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@supera.com")
                .name("Test User")
                .department(Department.TI)
                .build();

        testModule1 = Module.builder()
                .id(1L)
                .name("Portal do Colaborador")
                .active(true)
                .allowedDepartments(new HashSet<>(Arrays.asList(Department.TI, Department.FINANCEIRO)))
                .incompatibleModules(new HashSet<>())
                .build();

        testModule2 = Module.builder()
                .id(2L)
                .name("Relatórios Gerenciais")
                .active(true)
                .allowedDepartments(new HashSet<>(Arrays.asList(Department.TI, Department.FINANCEIRO)))
                .incompatibleModules(new HashSet<>())
                .build();

        requestDTO = new AccessRequestDTO();
        requestDTO.setModuleIds(Arrays.asList(1L));
        requestDTO.setJustification("Preciso deste módulo para realizar minhas atividades profissionais");
        requestDTO.setUrgent(false);
    }

    @Test
    void testCreateAccessRequestSuccess() {
        // Arrange
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(testUser));
        when(moduleRepository.findById(eq(1L))).thenReturn(Optional.of(testModule1));
        when(accessRequestRepository.findActiveRequestsByUserAndModule(eq(testUser), eq(testModule1)))
                .thenReturn(Collections.emptyList());
        when(userModuleRepository.findActiveModulesByUser(eq(testUser))).thenReturn(Collections.emptyList());
        when(accessRequestRepository.count()).thenReturn(0L);
        when(userModuleRepository.countActiveModulesByUser(eq(testUser))).thenReturn(0L);

        // Act
        String result = accessRequestService.createAccessRequest(1L, requestDTO);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Solicitação criada com sucesso"));
        verify(accessRequestRepository).save(any(AccessRequest.class));
        verify(userModuleRepository, times(1)).save(any(UserModule.class));
    }

    @Test
    void testCreateAccessRequestUserNotFound() {
        // Arrange
        when(userRepository.findById(eq(1L))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
                accessRequestService.createAccessRequest(1L, requestDTO));

        verify(userRepository).findById(eq(1L));
        verify(accessRequestRepository, never()).save(any());
    }

    @Test
    void testCreateAccessRequestModuleNotFound() {
        // Arrange
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(testUser));
        when(moduleRepository.findById(eq(1L))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
                accessRequestService.createAccessRequest(1L, requestDTO));

        verify(moduleRepository).findById(eq(1L));
    }

    @Test
    void testCreateAccessRequestModuleInactive() {
        // Arrange
        testModule1.setActive(false);
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(testUser));
        when(moduleRepository.findById(eq(1L))).thenReturn(Optional.of(testModule1));

        // Act & Assert
        assertThrows(BusinessException.class, () -> 
                accessRequestService.createAccessRequest(1L, requestDTO));

        verify(accessRequestRepository, never()).save(any());
    }

    @Test
    void testCreateAccessRequestActiveRequestExists() {
        // Arrange
        AccessRequest existingRequest = AccessRequest.builder()
                .id(1L)
                .status(RequestStatus.ATIVO)
                .build();
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(testUser));
        when(moduleRepository.findById(eq(1L))).thenReturn(Optional.of(testModule1));
        when(accessRequestRepository.findActiveRequestsByUserAndModule(eq(testUser), eq(testModule1)))
                .thenReturn(Arrays.asList(existingRequest));

        // Act & Assert
        assertThrows(BusinessException.class, () -> 
                accessRequestService.createAccessRequest(1L, requestDTO));

        verify(accessRequestRepository, never()).save(any());
    }

    @Test
    void testCreateAccessRequestUserAlreadyHasAccess() {
        // Arrange
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(testUser));
        when(moduleRepository.findById(eq(1L))).thenReturn(Optional.of(testModule1));
        when(accessRequestRepository.findActiveRequestsByUserAndModule(eq(testUser), eq(testModule1)))
                .thenReturn(Collections.emptyList());
        when(userModuleRepository.findActiveModulesByUser(eq(testUser)))
                .thenReturn(Arrays.asList(testModule1));

        // Act & Assert
        assertThrows(BusinessException.class, () -> 
                accessRequestService.createAccessRequest(1L, requestDTO));

        verify(accessRequestRepository, never()).save(any());
    }

    @Test
    void testCreateAccessRequestGenericJustification() {
        // Arrange
        requestDTO.setJustification("teste");
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(testUser));
        when(moduleRepository.findById(eq(1L))).thenReturn(Optional.of(testModule1));
        when(accessRequestRepository.findActiveRequestsByUserAndModule(eq(testUser), eq(testModule1)))
                .thenReturn(Collections.emptyList());
        when(userModuleRepository.findActiveModulesByUser(eq(testUser))).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(BusinessException.class, () -> 
                accessRequestService.createAccessRequest(1L, requestDTO));

        verify(accessRequestRepository, never()).save(any());
    }

    @Test
    void testCreateAccessRequestDepartmentNotAllowed() {
        // Arrange
        testUser.setDepartment(Department.OUTROS);
        testModule1.setAllowedDepartments(new HashSet<>(Arrays.asList(Department.TI, Department.FINANCEIRO)));
        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(testUser));
        when(moduleRepository.findById(eq(1L))).thenReturn(Optional.of(testModule1));
        when(accessRequestRepository.findActiveRequestsByUserAndModule(eq(testUser), eq(testModule1)))
                .thenReturn(Collections.emptyList());
        when(userModuleRepository.findActiveModulesByUser(eq(testUser))).thenReturn(Collections.emptyList());
        when(accessRequestRepository.count()).thenReturn(0L);

        // Act
        String result = accessRequestService.createAccessRequest(1L, requestDTO);

        // Assert
        assertTrue(result.contains("Solicitação negada"));
        assertTrue(result.contains("Departamento sem permissão"));
        verify(accessRequestRepository).save(any(AccessRequest.class));
    }

    @Test
    void testGetUserRequests() {
        // Arrange
        AccessRequest request = AccessRequest.builder()
                .id(1L)
                .protocol("SOL-20240101-0001")
                .user(testUser)
                .requestedModules(new HashSet<>(Arrays.asList(testModule1)))
                .justification("Test justification")
                .urgent(false)
                .status(RequestStatus.ATIVO)
                .requestDate(LocalDateTime.now())
                .build();

        Page<AccessRequest> page = new PageImpl<>(Arrays.asList(request));
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findById(eq(1L))).thenReturn(Optional.of(testUser));
        when(accessRequestRepository.findByUserWithFilters(
                eq(testUser), eq(null), eq(null), eq(null), eq(null), eq(null), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<AccessRequestResponseDTO> result = accessRequestService.getUserRequests(
                1L, null, null, null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findById(eq(1L));
    }

    @Test
    void testCancelRequest() {
        // Arrange
        AccessRequest request = AccessRequest.builder()
                .id(1L)
                .protocol("SOL-20240101-0001")
                .user(testUser)
                .requestedModules(new HashSet<>(Arrays.asList(testModule1)))
                .status(RequestStatus.ATIVO)
                .build();

        UserModule userModule = UserModule.builder()
                .id(1L)
                .user(testUser)
                .module(testModule1)
                .active(true)
                .build();

        when(accessRequestRepository.findById(eq(1L))).thenReturn(Optional.of(request));
        when(userModuleRepository.findByUserAndActiveTrue(eq(testUser)))
                .thenReturn(Arrays.asList(userModule));

        // Act
        accessRequestService.cancelRequest(1L, 1L, "Motivo do cancelamento");

        // Assert
        assertEquals(RequestStatus.CANCELADO, request.getStatus());
        assertFalse(userModule.getActive());
        verify(accessRequestRepository).save(eq(request));
        verify(userModuleRepository).save(eq(userModule));
    }
}

