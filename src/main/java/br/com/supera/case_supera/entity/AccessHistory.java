package br.com.supera.case_supera.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "access_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private AccessRequest accessRequest;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestStatus previousStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestStatus newStatus;

    @Column(nullable = false)
    private LocalDateTime changeDate;

    @Column(length = 500)
    private String reason;
}

