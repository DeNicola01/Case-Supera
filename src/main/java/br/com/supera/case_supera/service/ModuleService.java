package br.com.supera.case_supera.service;

import br.com.supera.case_supera.dto.ModuleResponseDTO;
import br.com.supera.case_supera.entity.Module;
import br.com.supera.case_supera.repository.ModuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ModuleService {

    private final ModuleRepository moduleRepository;

    public ModuleService(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    public List<ModuleResponseDTO> getAllModules() {
        List<Module> modules = moduleRepository.findAll();
        return modules.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ModuleResponseDTO> getAvailableModules() {
        List<Module> modules = moduleRepository.findByActiveTrue();
        return modules.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ModuleResponseDTO toDTO(Module module) {
        return ModuleResponseDTO.builder()
                .id(module.getId())
                .name(module.getName())
                .description(module.getDescription())
                .active(module.getActive())
                .allowedDepartments(module.getAllowedDepartments().stream()
                        .map(Enum::name)
                        .collect(Collectors.toList()))
                .incompatibleModules(module.getIncompatibleModules().stream()
                        .map(Module::getName)
                        .collect(Collectors.toList()))
                .build();
    }
}

