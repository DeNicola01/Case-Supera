package br.com.supera.case_supera.dto;

import br.com.supera.case_supera.entity.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessRequestResponseDTO {
    private Long id;
    private String protocol;
    private List<String> requestedModules;
    private String justification;
    private Boolean urgent;
    private RequestStatus status;
    private LocalDateTime requestDate;
    private LocalDateTime expirationDate;
    private String denialReason;
    private List<AccessHistoryDTO> history;
}

