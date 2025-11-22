package br.com.supera.case_supera.config;

import br.com.supera.case_supera.entity.Department;
import br.com.supera.case_supera.entity.Module;
import br.com.supera.case_supera.entity.User;
import br.com.supera.case_supera.repository.ModuleRepository;
import br.com.supera.case_supera.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
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

    @Autowired
    private EntityManager entityManager;

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
        // Criar todos os 10 módulos (verificar por nome para evitar duplicatas)
        try {
            System.out.println("Inicializando módulos...");
            
            // 1. Portal do Colaborador (todos os departamentos)
            Module portal = createOrUpdateModule(
                    "Portal do Colaborador",
                    "Portal principal para colaboradores",
                    new HashSet<>(Arrays.asList(
                            Department.TI, Department.FINANCEIRO, Department.RH,
                            Department.OPERACOES, Department.OUTROS)),
                    new HashSet<>()
            );

            // 2. Relatórios Gerenciais (todos os departamentos)
            Module relatorios = createOrUpdateModule(
                    "Relatórios Gerenciais",
                    "Sistema de relatórios gerenciais",
                    new HashSet<>(Arrays.asList(
                            Department.TI, Department.FINANCEIRO, Department.RH,
                            Department.OPERACOES, Department.OUTROS)),
                    new HashSet<>()
            );

            // 3. Gestão Financeira (Financeiro, TI)
            Module gestaoFinanceira = createOrUpdateModule(
                    "Gestão Financeira",
                    "Sistema de gestão financeira",
                    new HashSet<>(Arrays.asList(Department.FINANCEIRO, Department.TI)),
                    new HashSet<>()
            );

            // 4. Aprovador Financeiro (Financeiro, TI) *incompatível com #5
            Module aprovadorFinanceiro = createOrUpdateModule(
                    "Aprovador Financeiro",
                    "Módulo para aprovação de solicitações financeiras",
                    new HashSet<>(Arrays.asList(Department.FINANCEIRO, Department.TI)),
                    new HashSet<>()
            );

            // 5. Solicitante Financeiro (Financeiro, TI) *incompatível com #4
            Module solicitanteFinanceiro = createOrUpdateModule(
                    "Solicitante Financeiro",
                    "Módulo para solicitação de recursos financeiros",
                    new HashSet<>(Arrays.asList(Department.FINANCEIRO, Department.TI)),
                    new HashSet<>()
            );

            // Configurar incompatibilidade entre Aprovador Financeiro (#4) e Solicitante Financeiro (#5)
            configureIncompatibility(aprovadorFinanceiro, solicitanteFinanceiro);

            // 6. Administrador RH (RH, TI) *incompatível com #7
            Module adminRH = createOrUpdateModule(
                    "Administrador RH",
                    "Módulo administrativo de recursos humanos",
                    new HashSet<>(Arrays.asList(Department.RH, Department.TI)),
                    new HashSet<>()
            );

            // 7. Colaborador RH (RH, TI) *incompatível com #6
            Module colaboradorRH = createOrUpdateModule(
                    "Colaborador RH",
                    "Módulo para colaboradores do RH",
                    new HashSet<>(Arrays.asList(Department.RH, Department.TI)),
                    new HashSet<>()
            );

            // Configurar incompatibilidade entre Administrador RH (#6) e Colaborador RH (#7)
            configureIncompatibility(adminRH, colaboradorRH);

            // 8. Gestão de Estoque (Operações, TI)
            Module estoque = createOrUpdateModule(
                    "Gestão de Estoque",
                    "Sistema de gestão de estoque",
                    new HashSet<>(Arrays.asList(Department.OPERACOES, Department.TI)),
                    new HashSet<>()
            );

            // 9. Compras (Operações, TI)
            Module compras = createOrUpdateModule(
                    "Compras",
                    "Sistema de gestão de compras",
                    new HashSet<>(Arrays.asList(Department.OPERACOES, Department.TI)),
                    new HashSet<>()
            );

            // 10. Auditoria (apenas TI)
            Module auditoria = createOrUpdateModule(
                    "Auditoria",
                    "Módulo de auditoria do sistema",
                    new HashSet<>(Arrays.asList(Department.TI)),
                    new HashSet<>()
            );
            System.out.println("Módulos inicializados com sucesso!");
            
        } catch (Exception e) {
            System.err.println("Erro ao inicializar módulos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cria ou atualiza um módulo no banco de dados
     */
    private Module createOrUpdateModule(String name, String description, 
                                       Set<Department> allowedDepartments, 
                                       Set<Module> incompatibleModules) {
        return moduleRepository.findByName(name)
                .map(existing -> {
                    // Se o módulo já existe, atualiza apenas se necessário
                    existing.setDescription(description);
                    existing.setActive(true);
                    existing.setAllowedDepartments(allowedDepartments);
                    // Não atualiza incompatibleModules aqui, será feito depois
                    return moduleRepository.save(existing);
                })
                .orElseGet(() -> {
                    // Cria novo módulo
                    Module m = Module.builder()
                            .name(name)
                            .description(description)
                            .active(true)
                            .allowedDepartments(allowedDepartments)
                            .incompatibleModules(new HashSet<>())
                            .build();
                    return moduleRepository.save(m);
                });
    }

    /**
     * Configura incompatibilidade bidirecional entre dois módulos
     */
    private void configureIncompatibility(Module module1, Module module2) {
        try {
            // Fazer flush para garantir que os módulos estão persistidos
            entityManager.flush();
            entityManager.clear();
            
            // Recarregar os módulos para garantir que estão na sessão do Hibernate
            module1 = moduleRepository.findById(module1.getId()).orElse(module1);
            module2 = moduleRepository.findById(module2.getId()).orElse(module2);
            
            // Configurar incompatibilidade bidirecional
            Set<Module> module1Incompat = new HashSet<>(module1.getIncompatibleModules());
            if (!module1Incompat.contains(module2)) {
                module1Incompat.add(module2);
                module1.setIncompatibleModules(module1Incompat);
            }
            
            Set<Module> module2Incompat = new HashSet<>(module2.getIncompatibleModules());
            if (!module2Incompat.contains(module1)) {
                module2Incompat.add(module1);
                module2.setIncompatibleModules(module2Incompat);
            }
            
            moduleRepository.save(module1);
            moduleRepository.save(module2);
            
            System.out.println("Incompatibilidade configurada entre: " + module1.getName() + " <-> " + module2.getName());
        } catch (Exception e) {
            System.err.println("Erro ao configurar incompatibilidade entre " + module1.getName() + " e " + module2.getName() + ": " + e.getMessage());
        }
    }
}

