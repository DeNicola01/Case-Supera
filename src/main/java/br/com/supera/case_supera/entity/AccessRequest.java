package br.com.supera.case_supera.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "access_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String protocol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany
    @JoinTable(
        name = "request_modules",
        joinColumns = @JoinColumn(name = "request_id"),
        inverseJoinColumns = @JoinColumn(name = "module_id")
    )
    @Builder.Default
    private Set<Module> requestedModules = new HashSet<>();

    @Column(nullable = false, length = 500)
    private String justification;

    @Column(nullable = false)
    @Builder.Default
    private Boolean urgent = false;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RequestStatus status = RequestStatus.ATIVO;

    @Column(nullable = false)
    private LocalDateTime requestDate;

    private LocalDateTime expirationDate;

    private String denialReason;

    @OneToMany(mappedBy = "accessRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AccessHistory> history = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renewed_from_id")
    private AccessRequest renewedFrom;
}

