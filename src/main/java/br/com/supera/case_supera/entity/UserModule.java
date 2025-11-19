package br.com.supera.case_supera.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_modules", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "module_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Column(nullable = false)
    private LocalDateTime grantedDate;

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}

