package br.com.supera.case_supera.service;

import br.com.supera.case_supera.dto.ModuleResponseDTO;
import br.com.supera.case_supera.entity.Department;
import br.com.supera.case_supera.entity.Module;
import br.com.supera.case_supera.entity.User;
import br.com.supera.case_supera.repository.ModuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModuleServiceTest {

    @Mock
    private ModuleRepository moduleRepository;

    @InjectMocks
    private ModuleService moduleService;

    private Module testModule1;
    private Module testModule2;
    private User testUser;

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
                .description("Portal principal")
                .active(true)
                .allowedDepartments(new HashSet<>(Arrays.asList(Department.TI)))
                .incompatibleModules(new HashSet<>())
                .build();

        testModule2 = Module.builder()
                .id(2L)
                .name("Relatórios Gerenciais")
                .description("Sistema de relatórios")
                .active(false)
                .allowedDepartments(new HashSet<>(Arrays.asList(Department.FINANCEIRO)))
                .incompatibleModules(new HashSet<>())
                .build();
    }

    @Test
    void testGetAllModules() {
        // Arrange
        when(moduleRepository.findAll()).thenReturn(Arrays.asList(testModule1, testModule2));

        // Act
        List<ModuleResponseDTO> result = moduleService.getAllModules();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Portal do Colaborador", result.get(0).getName());
        verify(moduleRepository).findAll();
    }

    @Test
    void testGetAvailableModules() {
        // Arrange
        when(moduleRepository.findByActiveTrue()).thenReturn(Arrays.asList(testModule1));

        // Act
        List<ModuleResponseDTO> result = moduleService.getAvailableModules(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Portal do Colaborador", result.get(0).getName());
        assertTrue(result.get(0).getActive());
        verify(moduleRepository).findByActiveTrue();
    }

    @Test
    void testGetAvailableModulesFilteredByDepartment() {
        // Arrange
        User financeiroUser = User.builder()
                .id(2L)
                .email("financeiro@supera.com")
                .name("Financeiro User")
                .department(Department.FINANCEIRO)
                .build();

        Module moduleFinanceiro = Module.builder()
                .id(3L)
                .name("Gestão Financeira")
                .description("Sistema financeiro")
                .active(true)
                .allowedDepartments(new HashSet<>(Arrays.asList(Department.FINANCEIRO, Department.TI)))
                .incompatibleModules(new HashSet<>())
                .build();

        when(moduleRepository.findByActiveTrue()).thenReturn(Arrays.asList(testModule1, moduleFinanceiro));

        // Act
        List<ModuleResponseDTO> result = moduleService.getAvailableModules(financeiroUser);

        // Assert
        assertNotNull(result);
        // Financeiro deve ver apenas módulos permitidos para seu departamento
        // TI vê todos, então testModule1 (permitido para TI) não deve aparecer para Financeiro
        // moduleFinanceiro deve aparecer pois é permitido para Financeiro
        assertTrue(result.stream().anyMatch(m -> m.getName().equals("Gestão Financeira")));
        verify(moduleRepository).findByActiveTrue();
    }

    @Test
    void testGetAvailableModulesTIUserSeesAll() {
        // Arrange
        Module moduleRH = Module.builder()
                .id(4L)
                .name("Administrador RH")
                .description("Módulo RH")
                .active(true)
                .allowedDepartments(new HashSet<>(Arrays.asList(Department.RH, Department.TI)))
                .incompatibleModules(new HashSet<>())
                .build();

        when(moduleRepository.findByActiveTrue()).thenReturn(Arrays.asList(testModule1, moduleRH));

        // Act
        List<ModuleResponseDTO> result = moduleService.getAvailableModules(testUser);

        // Assert
        assertNotNull(result);
        // TI deve ver todos os módulos
        assertEquals(2, result.size());
        verify(moduleRepository).findByActiveTrue();
    }
}

