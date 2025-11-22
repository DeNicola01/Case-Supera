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
                            Department.OPERACOES, Department.OUTROS))
            );

            // 2. Relatórios Gerenciais (todos os departamentos)
            Module relatorios = createOrUpdateModule(
                    "Relatórios Gerenciais",
                    "Sistema de relatórios gerenciais",
                    new HashSet<>(Arrays.asList(
                            Department.TI, Department.FINANCEIRO, Department.RH,
                            Department.OPERACOES, Department.OUTROS))
            );

            // 3. Gestão Financeira (Financeiro, TI)
            Module gestaoFinanceira = createOrUpdateModule(
                    "Gestão Financeira",
                    "Sistema de gestão financeira",
                    new HashSet<>(Arrays.asList(Department.FINANCEIRO, Department.TI))
            );

            // 4. Aprovador Financeiro (Financeiro, TI) *incompatível com #5
            Module aprovadorFinanceiro = createOrUpdateModule(
                    "Aprovador Financeiro",
                    "Módulo para aprovação de solicitações financeiras",
                    new HashSet<>(Arrays.asList(Department.FINANCEIRO, Department.TI))
            );

            // 5. Solicitante Financeiro (Financeiro, TI) *incompatível com #4
            Module solicitanteFinanceiro = createOrUpdateModule(
                    "Solicitante Financeiro",
                    "Módulo para solicitação de recursos financeiros",
                    new HashSet<>(Arrays.asList(Department.FINANCEIRO, Department.TI))
            );

            // 6. Administrador RH (RH, TI) *incompatível com #7
            Module adminRH = createOrUpdateModule(
                    "Administrador RH",
                    "Módulo administrativo de recursos humanos",
                    new HashSet<>(Arrays.asList(Department.RH, Department.TI))
            );

            // 7. Colaborador RH (RH, TI) *incompatível com #6
            Module colaboradorRH = createOrUpdateModule(
                    "Colaborador RH",
                    "Módulo para colaboradores do RH",
                    new HashSet<>(Arrays.asList(Department.RH, Department.TI))
            );

            // 8. Gestão de Estoque (Operações, TI)
            Module estoque = createOrUpdateModule(
                    "Gestão de Estoque",
                    "Sistema de gestão de estoque",
                    new HashSet<>(Arrays.asList(Department.OPERACOES, Department.TI))
            );

            // 9. Compras (Operações, TI)
            Module compras = createOrUpdateModule(
                    "Compras",
                    "Sistema de gestão de compras",
                    new HashSet<>(Arrays.asList(Department.OPERACOES, Department.TI))
            );

            // 10. Auditoria (apenas TI)
            Module auditoria = createOrUpdateModule(
                    "Auditoria",
                    "Módulo de auditoria do sistema",
                    new HashSet<>(Arrays.asList(Department.TI))
            );

            // Configurar incompatibilidades diretamente na tabela module_incompatibilities
            System.out.println("Configurando incompatibilidades na tabela...");
            
            System.out.println("IDs dos módulos:");
            System.out.println("  Aprovador Financeiro: " + aprovadorFinanceiro.getId());
            System.out.println("  Solicitante Financeiro: " + solicitanteFinanceiro.getId());
            System.out.println("  Administrador RH: " + adminRH.getId());
            System.out.println("  Colaborador RH: " + colaboradorRH.getId());

            // Limpar incompatibilidades existentes para evitar duplicatas
            int deleted = entityManager.createNativeQuery(
                    "DELETE FROM module_incompatibilities"
            ).executeUpdate();
            System.out.println("Registros deletados: " + deleted);

            // Inserir incompatibilidades diretamente na tabela
            // 1. Aprovador Financeiro -> Solicitante Financeiro
            int result1 = entityManager.createNativeQuery(
                    "INSERT INTO module_incompatibilities (module_id, incompatible_module_id) " +
                    "SELECT ?::bigint, ?::bigint " +
                    "WHERE NOT EXISTS (" +
                    "  SELECT 1 FROM module_incompatibilities " +
                    "  WHERE module_id = ?::bigint AND incompatible_module_id = ?::bigint" +
                    ")"
            )
            .setParameter(1, aprovadorFinanceiro.getId())
            .setParameter(2, solicitanteFinanceiro.getId())
            .setParameter(3, aprovadorFinanceiro.getId())
            .setParameter(4, solicitanteFinanceiro.getId())
            .executeUpdate();
            System.out.println("Inserido Aprovador -> Solicitante: " + result1);

            // 2. Solicitante Financeiro -> Aprovador Financeiro
            int result2 = entityManager.createNativeQuery(
                    "INSERT INTO module_incompatibilities (module_id, incompatible_module_id) " +
                    "SELECT ?::bigint, ?::bigint " +
                    "WHERE NOT EXISTS (" +
                    "  SELECT 1 FROM module_incompatibilities " +
                    "  WHERE module_id = ?::bigint AND incompatible_module_id = ?::bigint" +
                    ")"
            )
            .setParameter(1, solicitanteFinanceiro.getId())
            .setParameter(2, aprovadorFinanceiro.getId())
            .setParameter(3, solicitanteFinanceiro.getId())
            .setParameter(4, aprovadorFinanceiro.getId())
            .executeUpdate();
            System.out.println("Inserido Solicitante -> Aprovador: " + result2);

            // 3. Administrador RH -> Colaborador RH
            int result3 = entityManager.createNativeQuery(
                    "INSERT INTO module_incompatibilities (module_id, incompatible_module_id) " +
                    "SELECT ?::bigint, ?::bigint " +
                    "WHERE NOT EXISTS (" +
                    "  SELECT 1 FROM module_incompatibilities " +
                    "  WHERE module_id = ?::bigint AND incompatible_module_id = ?::bigint" +
                    ")"
            )
            .setParameter(1, adminRH.getId())
            .setParameter(2, colaboradorRH.getId())
            .setParameter(3, adminRH.getId())
            .setParameter(4, colaboradorRH.getId())
            .executeUpdate();
            System.out.println("Inserido Admin RH -> Colaborador RH: " + result3);

            // 4. Colaborador RH -> Administrador RH
            int result4 = entityManager.createNativeQuery(
                    "INSERT INTO module_incompatibilities (module_id, incompatible_module_id) " +
                    "SELECT ?::bigint, ?::bigint " +
                    "WHERE NOT EXISTS (" +
                    "  SELECT 1 FROM module_incompatibilities " +
                    "  WHERE module_id = ?::bigint AND incompatible_module_id = ?::bigint" +
                    ")"
            )
            .setParameter(1, colaboradorRH.getId())
            .setParameter(2, adminRH.getId())
            .setParameter(3, colaboradorRH.getId())
            .setParameter(4, adminRH.getId())
            .executeUpdate();
            System.out.println("Inserido Colaborador RH -> Admin RH: " + result4);

            entityManager.flush();
            
            System.out.println("Incompatibilidades inseridas na tabela module_incompatibilities:");
            System.out.println("  - Aprovador Financeiro (ID: " + aprovadorFinanceiro.getId() + ") <-> Solicitante Financeiro (ID: " + solicitanteFinanceiro.getId() + ")");
            System.out.println("  - Administrador RH (ID: " + adminRH.getId() + ") <-> Colaborador RH (ID: " + colaboradorRH.getId() + ")");
            
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
                                       Set<Department> allowedDepartments) {
        return moduleRepository.findByName(name)
                .map(existing -> {
                    // Se o módulo já existe, atualiza apenas se necessário
                    existing.setDescription(description);
                    existing.setActive(true);
                    existing.setAllowedDepartments(allowedDepartments);
                    // Limpar incompatibilidades existentes - serão reconfiguradas depois
                    existing.setIncompatibleModules(new HashSet<>());
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

}

