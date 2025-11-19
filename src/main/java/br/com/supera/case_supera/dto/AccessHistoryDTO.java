package br.com.supera.case_supera.dto;

import br.com.supera.case_supera.entity.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessHistoryDTO {
    private RequestStatus previousStatus;
    private RequestStatus newStatus;
    private LocalDateTime changeDate;
    private String reason;
}

