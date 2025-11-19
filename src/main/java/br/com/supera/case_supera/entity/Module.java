package br.com.supera.case_supera.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "modules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @ElementCollection
    @CollectionTable(name = "module_allowed_departments", joinColumns = @JoinColumn(name = "module_id"))
    @Column(name = "department")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<Department> allowedDepartments = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "module_incompatibilities",
        joinColumns = @JoinColumn(name = "module_id"),
        inverseJoinColumns = @JoinColumn(name = "incompatible_module_id")
    )
    @Builder.Default
    private Set<Module> incompatibleModules = new HashSet<>();
}

