package com.dtc.busmanagement.service.impl;

import com.dtc.busmanagement.dto.DriverDTO;
import com.dtc.busmanagement.entity.*;
import com.dtc.busmanagement.enums.DriverStatus;
import com.dtc.busmanagement.enums.RoleName;
import com.dtc.busmanagement.exception.BadRequestException;
import com.dtc.busmanagement.exception.ResourceNotFoundException;
import com.dtc.busmanagement.repository.*;
import com.dtc.busmanagement.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DriverServiceImpl implements DriverService {

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepotRepository depotRepository;

    @Autowired
    private BusRepository busRepository;

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

    private DriverDTO convertToDTO(Driver driver) {
        DriverDTO dto = new DriverDTO();
        dto.setId(driver.getId());
        dto.setUserId(driver.getUser().getId());
        dto.setUsername(driver.getUser().getUsername());
        dto.setEmail(driver.getUser().getEmail());
        dto.setFullName(driver.getUser().getFullName());
        dto.setPhone(driver.getUser().getPhone());
        dto.setLicenseNumber(driver.getLicenseNumber());
        dto.setLicenseExpiryDate(driver.getLicenseExpiryDate());
        dto.setExperienceYears(driver.getExperienceYears());
        dto.setStatus(driver.getStatus().name());
        if (driver.getDepot() != null) {
            dto.setDepotId(driver.getDepot().getId());
            dto.setDepotName(driver.getDepot().getName());
        }
        if (driver.getAssignedBus() != null) {
            dto.setAssignedBusId(driver.getAssignedBus().getId());
            dto.setAssignedBusNumber(driver.getAssignedBus().getBusNumber());
        }
        return dto;
    }

    @Override
    public List<DriverDTO> getAllDrivers(String search) {
        List<Driver> drivers;
        if (search != null && !search.trim().isEmpty()) {
            drivers = driverRepository.searchDrivers(search);
        } else {
            drivers = driverRepository.findAll();
        }
        return drivers.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public DriverDTO getDriverById(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));
        return convertToDTO(driver);
    }

    @Override
    @Transactional
    public DriverDTO createDriver(DriverDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BadRequestException("Username " + dto.getUsername() + " is already taken.");
        }
        if (driverRepository.searchDrivers(dto.getLicenseNumber()).stream().anyMatch(d -> d.getLicenseNumber().equalsIgnoreCase(dto.getLicenseNumber()))) {
            throw new BadRequestException("License number " + dto.getLicenseNumber() + " already exists.");
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

        Role role = roleRepository.findByName(RoleName.ROLE_DRIVER)
                .orElseThrow(() -> new ResourceNotFoundException("Role ROLE_DRIVER not found"));
        user.setRole(role);

        // 2. Create Driver linked to User
        Driver driver = new Driver();
        driver.setUser(user);
        driver.setLicenseNumber(dto.getLicenseNumber());
        driver.setLicenseExpiryDate(dto.getLicenseExpiryDate());
        driver.setExperienceYears(dto.getExperienceYears());
        driver.setStatus(DriverStatus.valueOf(dto.getStatus().toUpperCase()));

        if (dto.getDepotId() != null) {
            Depot depot = depotRepository.findById(dto.getDepotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Depot not found with id: " + dto.getDepotId()));
            driver.setDepot(depot);
        }
        if (dto.getAssignedBusId() != null) {
            Bus bus = busRepository.findById(dto.getAssignedBusId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bus not found with id: " + dto.getAssignedBusId()));
            driver.setAssignedBus(bus);
        }

        Driver savedDriver = driverRepository.save(driver);

        // Audit Log
        AuditLog log = new AuditLog(getCurrentUsername(), "DRIVER_CREATE", "Added driver " + savedDriver.getUser().getFullName(), "127.0.0.1");
        auditLogRepository.save(log);

        return convertToDTO(savedDriver);
    }

    @Override
    @Transactional
    public DriverDTO updateDriver(Long id, DriverDTO dto) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));

        // Update User Details
        User user = driver.getUser();
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }

        // Update Driver details
        driver.setLicenseNumber(dto.getLicenseNumber());
        driver.setLicenseExpiryDate(dto.getLicenseExpiryDate());
        driver.setExperienceYears(dto.getExperienceYears());
        driver.setStatus(DriverStatus.valueOf(dto.getStatus().toUpperCase()));

        if (dto.getDepotId() != null) {
            Depot depot = depotRepository.findById(dto.getDepotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Depot not found with id: " + dto.getDepotId()));
            driver.setDepot(depot);
        } else {
            driver.setDepot(null);
        }

        if (dto.getAssignedBusId() != null) {
            Bus bus = busRepository.findById(dto.getAssignedBusId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bus not found with id: " + dto.getAssignedBusId()));
            driver.setAssignedBus(bus);
        } else {
            driver.setAssignedBus(null);
        }

        Driver updatedDriver = driverRepository.save(driver);

        // Audit Log
        AuditLog log = new AuditLog(getCurrentUsername(), "DRIVER_UPDATE", "Updated driver " + updatedDriver.getUser().getFullName(), "127.0.0.1");
        auditLogRepository.save(log);

        return convertToDTO(updatedDriver);
    }

    @Override
    @Transactional
    public void deleteDriver(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));
        driverRepository.delete(driver);

        // Audit Log
        AuditLog log = new AuditLog(getCurrentUsername(), "DRIVER_DELETE", "Deleted driver " + driver.getUser().getFullName(), "127.0.0.1");
        auditLogRepository.save(log);
    }

    @Override
    public List<DriverDTO> getDriversByStatus(String status) {
        DriverStatus driverStatus = DriverStatus.valueOf(status.toUpperCase());
        return driverRepository.findByStatus(driverStatus).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<DriverDTO> getDriversByDepot(Long depotId) {
        return driverRepository.findByDepotId(depotId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<DriverDTO> getLicenseExpiringDrivers() {
        // Licenses expiring in next 30 days
        LocalDate limitDate = LocalDate.now().plusDays(30);
        return driverRepository.findDriversWithLicenseExpiringBefore(limitDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
