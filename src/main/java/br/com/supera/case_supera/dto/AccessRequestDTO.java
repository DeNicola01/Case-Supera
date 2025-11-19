package br.com.supera.case_supera.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AccessRequestDTO {
    @NotEmpty(message = "Pelo menos um módulo deve ser solicitado")
    @Size(min = 1, max = 3, message = "Deve solicitar entre 1 e 3 módulos")
    private List<Long> moduleIds;

    @NotEmpty(message = "Justificativa é obrigatória")
    @Size(min = 20, max = 500, message = "Justificativa deve ter entre 20 e 500 caracteres")
    private String justification;

    private Boolean urgent = false;
}

