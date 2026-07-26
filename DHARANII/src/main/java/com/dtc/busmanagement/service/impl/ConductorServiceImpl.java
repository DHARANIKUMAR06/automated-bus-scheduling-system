package com.dtc.busmanagement.service.impl;

import com.dtc.busmanagement.dto.ConductorDTO;
import com.dtc.busmanagement.entity.*;
import com.dtc.busmanagement.enums.ConductorStatus;
import com.dtc.busmanagement.enums.RoleName;
import com.dtc.busmanagement.exception.BadRequestException;
import com.dtc.busmanagement.exception.ResourceNotFoundException;
import com.dtc.busmanagement.repository.*;
import com.dtc.busmanagement.service.ConductorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConductorServiceImpl implements ConductorService {

    @Autowired
    private ConductorRepository conductorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepotRepository depotRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String getCurrentUsername() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return "system";
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private ConductorDTO convertToDTO(Conductor conductor) {
        ConductorDTO dto = new ConductorDTO();
        dto.setId(conductor.getId());
        dto.setUserId(conductor.getUser().getId());
        dto.setUsername(conductor.getUser().getUsername());
        dto.setEmail(conductor.getUser().getEmail());
        dto.setFullName(conductor.getUser().getFullName());
        dto.setPhone(conductor.getUser().getPhone());
        dto.setEmployeeId(conductor.getEmployeeId());
        dto.setStatus(conductor.getStatus().name());
        if (conductor.getDepot() != null) {
            dto.setDepotId(conductor.getDepot().getId());
            dto.setDepotName(conductor.getDepot().getName());
        }
        return dto;
    }

    @Override
    public List<ConductorDTO> getAllConductors(String search) {
        List<Conductor> conductors;
        if (search != null && !search.trim().isEmpty()) {
            conductors = conductorRepository.searchConductors(search);
        } else {
            conductors = conductorRepository.findAll();
        }
        return conductors.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public ConductorDTO getConductorById(Long id) {
        Conductor conductor = conductorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor not found with id: " + id));
        return convertToDTO(conductor);
    }

    @Override
    @Transactional
    public ConductorDTO createConductor(ConductorDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BadRequestException("Username " + dto.getUsername() + " is already taken.");
        }
        if (conductorRepository.findByEmployeeId(dto.getEmployeeId()).isPresent()) {
            throw new BadRequestException("Employee ID " + dto.getEmployeeId() + " already exists.");
        }

        // 1. Create Base User
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail() != null ? dto.getEmail() : dto.getUsername() + "@dtc.delhi.gov.in");
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setStatus("ACTIVE");

        String rawPassword = dto.getPassword() != null ? dto.getPassword() : "admin123";
        user.setPassword(passwordEncoder.encode(rawPassword));

        Role role = roleRepository.findByName(RoleName.ROLE_CONDUCTOR)
                .orElseThrow(() -> new ResourceNotFoundException("Role ROLE_CONDUCTOR not found"));
        user.setRole(role);

        // 2. Create Conductor linked to User
        Conductor conductor = new Conductor();
        conductor.setUser(user);
        conductor.setEmployeeId(dto.getEmployeeId());
        conductor.setStatus(ConductorStatus.valueOf(dto.getStatus().toUpperCase()));

        if (dto.getDepotId() != null) {
            Depot depot = depotRepository.findById(dto.getDepotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Depot not found with id: " + dto.getDepotId()));
            conductor.setDepot(depot);
        }

        Conductor savedConductor = conductorRepository.save(conductor);

        // Audit Log
        AuditLog log = new AuditLog(getCurrentUsername(), "CONDUCTOR_CREATE", "Added conductor " + savedConductor.getUser().getFullName(), "127.0.0.1");
        auditLogRepository.save(log);

        return convertToDTO(savedConductor);
    }

    @Override
    @Transactional
    public ConductorDTO updateConductor(Long id, ConductorDTO dto) {
        Conductor conductor = conductorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor not found with id: " + id));

        // Update User Details
        User user = conductor.getUser();
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }

        // Update Conductor details
        conductor.setEmployeeId(dto.getEmployeeId());
        conductor.setStatus(ConductorStatus.valueOf(dto.getStatus().toUpperCase()));

        if (dto.getDepotId() != null) {
            Depot depot = depotRepository.findById(dto.getDepotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Depot not found with id: " + dto.getDepotId()));
            conductor.setDepot(depot);
        } else {
            conductor.setDepot(null);
        }

        Conductor updatedConductor = conductorRepository.save(conductor);

        // Audit Log
        AuditLog log = new AuditLog(getCurrentUsername(), "CONDUCTOR_UPDATE", "Updated conductor " + updatedConductor.getUser().getFullName(), "127.0.0.1");
        auditLogRepository.save(log);

        return convertToDTO(updatedConductor);
    }

    @Override
    @Transactional
    public void deleteConductor(Long id) {
        Conductor conductor = conductorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor not found with id: " + id));
        conductorRepository.delete(conductor);

        // Audit Log
        AuditLog log = new AuditLog(getCurrentUsername(), "CONDUCTOR_DELETE", "Deleted conductor " + conductor.getUser().getFullName(), "127.0.0.1");
        auditLogRepository.save(log);
    }

    @Override
    public List<ConductorDTO> getConductorsByStatus(String status) {
        ConductorStatus condStatus = ConductorStatus.valueOf(status.toUpperCase());
        return conductorRepository.findByStatus(condStatus).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ConductorDTO> getConductorsByDepot(Long depotId) {
        return conductorRepository.findByDepotId(depotId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }
}
