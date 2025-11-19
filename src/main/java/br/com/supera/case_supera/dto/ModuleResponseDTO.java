package br.com.supera.case_supera.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleResponseDTO {
    private Long id;
    private String name;
    private String description;
    private Boolean active;
    private List<String> allowedDepartments;
    private List<String> incompatibleModules;
}

