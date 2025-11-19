package br.com.supera.case_supera.config;

import br.com.supera.case_supera.entity.Department;
import br.com.supera.case_supera.entity.Module;
import br.com.supera.case_supera.entity.User;
import br.com.supera.case_supera.repository.ModuleRepository;
import br.com.supera.case_supera.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @PostConstruct
    @Transactional
    public void init() {
        initUsers();
        initModules();
    }

    private void initUsers() {
        if (userRepository.count() == 0) {
            List<User> users = Arrays.asList(
                    User.builder()
                            .email("ti@supera.com")
                            .password("senha123") // Será criptografado automaticamente pelo @PrePersist
                            .name("João Silva - TI")
                            .department(Department.TI)
                            .build(),
                    User.builder()
                            .email("financeiro@supera.com")
                            .password("senha123")
                            .name("Maria Santos - Financeiro")
                            .department(Department.FINANCEIRO)
                            .build(),
                    User.builder()
                            .email("rh@supera.com")
                            .password("senha123")
                            .name("Pedro Oliveira - RH")
                            .department(Department.RH)
                            .build(),
                    User.builder()
                            .email("operacoes@supera.com")
                            .password("senha123")
                            .name("Ana Costa - Operações")
                            .department(Department.OPERACOES)
                            .build(),
                    User.builder()
                            .email("outros@supera.com")
                            .password("senha123")
                            .name("Carlos Mendes - Outros")
                            .department(Department.OUTROS)
                            .build()
            );

            userRepository.saveAll(users);
        }
    }

    private void initModules() {
        if (moduleRepository.count() == 0) {
            // Portal do Colaborador
            Module portal = Module.builder()
                    .name("Portal do Colaborador")
                    .description("Portal principal para colaboradores")
                    .active(true)
                    .allowedDepartments(new HashSet<>(Arrays.asList(
                            Department.TI, Department.FINANCEIRO, Department.RH,
                            Department.OPERACOES, Department.OUTROS)))
                    .incompatibleModules(new HashSet<>())
                    .build();
            portal = moduleRepository.save(portal);

            // Relatórios Gerenciais
            Module relatorios = Module.builder()
                    .name("Relatórios Gerenciais")
                    .description("Sistema de relatórios gerenciais")
                    .active(true)
                    .allowedDepartments(new HashSet<>(Arrays.asList(
                            Department.TI, Department.FINANCEIRO, Department.RH,
                            Department.OPERACOES, Department.OUTROS)))
                    .incompatibleModules(new HashSet<>())
                    .build();
            relatorios = moduleRepository.save(relatorios);

            // Gestão Financeira
            Module gestaoFinanceira = Module.builder()
                    .name("Gestão Financeira")
                    .description("Sistema de gestão financeira")
                    .active(true)
                    .allowedDepartments(new HashSet<>(Arrays.asList(Department.FINANCEIRO, Department.TI)))
                    .incompatibleModules(new HashSet<>())
                    .build();
            gestaoFinanceira = moduleRepository.save(gestaoFinanceira);

            // Aprovador Financeiro
            Module aprovadorFinanceiro = Module.builder()
                    .name("Aprovador Financeiro")
                    .description("Módulo para aprovação de solicitações financeiras")
                    .active(true)
                    .allowedDepartments(new HashSet<>(Arrays.asList(Department.FINANCEIRO, Department.TI)))
                    .incompatibleModules(new HashSet<>())
                    .build();
            aprovadorFinanceiro = moduleRepository.save(aprovadorFinanceiro);

            // Solicitante Financeiro
            Module solicitanteFinanceiro = Module.builder()
                    .name("Solicitante Financeiro")
                    .description("Módulo para solicitação de recursos financeiros")
                    .active(true)
                    .allowedDepartments(new HashSet<>(Arrays.asList(Department.FINANCEIRO, Department.TI)))
                    .incompatibleModules(new HashSet<>())
                    .build();
            solicitanteFinanceiro = moduleRepository.save(solicitanteFinanceiro);

            // Configurar incompatibilidade entre Aprovador e Solicitante
            aprovadorFinanceiro.getIncompatibleModules().add(solicitanteFinanceiro);
            solicitanteFinanceiro.getIncompatibleModules().add(aprovadorFinanceiro);
            moduleRepository.save(aprovadorFinanceiro);
            moduleRepository.save(solicitanteFinanceiro);

            // Administrador RH
            Module adminRH = Module.builder()
                    .name("Administrador RH")
                    .description("Módulo administrativo de recursos humanos")
                    .active(true)
                    .allowedDepartments(new HashSet<>(Arrays.asList(Department.RH, Department.TI)))
                    .incompatibleModules(new HashSet<>())
                    .build();
            adminRH = moduleRepository.save(adminRH);

            // Colaborador RH
            Module colaboradorRH = Module.builder()
                    .name("Colaborador RH")
                    .description("Módulo para colaboradores do RH")
                    .active(true)
                    .allowedDepartments(new HashSet<>(Arrays.asList(Department.RH, Department.TI)))
                    .incompatibleModules(new HashSet<>())
                    .build();
            colaboradorRH = moduleRepository.save(colaboradorRH);

            // Configurar incompatibilidade entre Admin RH e Colaborador RH
            adminRH.getIncompatibleModules().add(colaboradorRH);
            colaboradorRH.getIncompatibleModules().add(adminRH);
            moduleRepository.save(adminRH);
            moduleRepository.save(colaboradorRH);

            // Gestão de Estoque
            Module estoque = Module.builder()
                    .name("Gestão de Estoque")
                    .description("Sistema de gestão de estoque")
                    .active(true)
                    .allowedDepartments(new HashSet<>(Arrays.asList(Department.OPERACOES, Department.TI)))
                    .incompatibleModules(new HashSet<>())
                    .build();
            moduleRepository.save(estoque);

            // Compras
            Module compras = Module.builder()
                    .name("Compras")
                    .description("Sistema de gestão de compras")
                    .active(true)
                    .allowedDepartments(new HashSet<>(Arrays.asList(Department.OPERACOES, Department.TI)))
                    .incompatibleModules(new HashSet<>())
                    .build();
            moduleRepository.save(compras);

            // Auditoria
            Module auditoria = Module.builder()
                    .name("Auditoria")
                    .description("Módulo de auditoria do sistema")
                    .active(true)
                    .allowedDepartments(new HashSet<>(Arrays.asList(Department.TI)))
                    .incompatibleModules(new HashSet<>())
                    .build();
            moduleRepository.save(auditoria);
        }
    }
}

