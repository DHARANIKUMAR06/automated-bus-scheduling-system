package com.dtc.busmanagement.service.impl;

import com.dtc.busmanagement.dto.MaintenanceDTO;
import com.dtc.busmanagement.entity.*;
import com.dtc.busmanagement.enums.*;
import com.dtc.busmanagement.exception.ResourceNotFoundException;
import com.dtc.busmanagement.repository.*;
import com.dtc.busmanagement.service.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaintenanceServiceImpl implements MaintenanceService {

    @Autowired
    private MaintenanceRecordRepository maintenanceRepository;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private String getCurrentUsername() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return "system";
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private MaintenanceDTO convertToDTO(MaintenanceRecord m) {
        MaintenanceDTO dto = new MaintenanceDTO();
        dto.setId(m.getId());
        dto.setBusId(m.getBus().getId());
        dto.setBusNumber(m.getBus().getBusNumber());
        dto.setServiceDate(m.getServiceDate());
        dto.setServiceType(m.getServiceType());
        dto.setDescription(m.getDescription());
        dto.setCost(m.getCost());
        dto.setStatus(m.getStatus().name());
        dto.setTechnicianNotes(m.getTechnicianNotes());
        return dto;
    }

    @Override
    public List<MaintenanceDTO> getAllMaintenance() {
        return maintenanceRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public MaintenanceDTO getMaintenanceById(Long id) {
        MaintenanceRecord m = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance record not found with id: " + id));
        return convertToDTO(m);
    }

    @Override
    @Transactional
    public MaintenanceDTO createMaintenance(MaintenanceDTO dto) {
        Bus bus = busRepository.findById(dto.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found with id: " + dto.getBusId()));

        MaintenanceRecord m = new MaintenanceRecord();
        m.setBus(bus);
        m.setServiceDate(dto.getServiceDate());
        m.setServiceType(dto.getServiceType());
        m.setDescription(dto.getDescription());
        m.setCost(dto.getCost());
        
        MaintenanceStatus mStatus = MaintenanceStatus.valueOf(dto.getStatus().toUpperCase());
        m.setStatus(mStatus);
        m.setTechnicianNotes(dto.getTechnicianNotes());

        // If service is in progress, put bus in maintenance status
        if (mStatus == MaintenanceStatus.IN_PROGRESS) {
            bus.setStatus(BusStatus.MAINTENANCE);
            busRepository.save(bus);
        }

        MaintenanceRecord saved = maintenanceRepository.save(m);

        // Audit Log
        AuditLog log = new AuditLog(getCurrentUsername(), "MAINTENANCE_CREATE", 
                "Created maintenance schedule for Bus " + bus.getBusNumber() + " on " + dto.getServiceDate(), 
                "127.0.0.1");
        auditLogRepository.save(log);

        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public MaintenanceDTO updateMaintenance(Long id, MaintenanceDTO dto) {
        MaintenanceRecord m = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance record not found"));

        Bus bus = busRepository.findById(dto.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));

        m.setBus(bus);
        m.setServiceDate(dto.getServiceDate());
        m.setServiceType(dto.getServiceType());
        m.setDescription(dto.getDescription());
        m.setCost(dto.getCost());
        
        MaintenanceStatus newStatus = MaintenanceStatus.valueOf(dto.getStatus().toUpperCase());
        m.setStatus(newStatus);
        m.setTechnicianNotes(dto.getTechnicianNotes());

        // If status changes to IN_PROGRESS, update bus to MAINTENANCE
        if (newStatus == MaintenanceStatus.IN_PROGRESS) {
            bus.setStatus(BusStatus.MAINTENANCE);
            busRepository.save(bus);
        }
        // If status changes to COMPLETED, restore bus status to ACTIVE and record service date
        else if (newStatus == MaintenanceStatus.COMPLETED) {
            bus.setStatus(BusStatus.ACTIVE);
            bus.setLastServiceDate(dto.getServiceDate());
            busRepository.save(bus);
        }

        MaintenanceRecord updated = maintenanceRepository.save(m);

        // Audit Log
        AuditLog log = new AuditLog(getCurrentUsername(), "MAINTENANCE_UPDATE", 
                "Updated maintenance record ID " + id + " for Bus " + bus.getBusNumber(), 
                "127.0.0.1");
        auditLogRepository.save(log);

        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteMaintenance(Long id) {
        MaintenanceRecord m = maintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance record not found"));
        
        // If it was in progress, free up the bus status
        if (m.getStatus() == MaintenanceStatus.IN_PROGRESS) {
            Bus bus = m.getBus();
            bus.setStatus(BusStatus.ACTIVE);
            busRepository.save(bus);
        }

        maintenanceRepository.delete(m);

        // Audit Log
        AuditLog log = new AuditLog(getCurrentUsername(), "MAINTENANCE_DELETE", 
                "Deleted maintenance record ID " + id + " for Bus " + m.getBus().getBusNumber(), 
                "127.0.0.1");
        auditLogRepository.save(log);
    }

    @Override
    public List<MaintenanceDTO> getMaintenanceByBus(Long busId) {
        return maintenanceRepository.findByBusId(busId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<MaintenanceDTO> getUpcomingMaintenance() {
        LocalDate nextWeek = LocalDate.now().plusDays(7);
        return maintenanceRepository.findUpcomingMaintenance(nextWeek).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Cron job runs every midnight to trigger alerts for upcoming servicing and check fleet health
    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void triggerMaintenanceScheduler() {
        LocalDate targetDate = LocalDate.now().plusDays(3); // alert for maintenance scheduled in next 3 days
        List<MaintenanceRecord> upcoming = maintenanceRepository.findUpcomingMaintenance(targetDate);
        
        List<User> staffToNotify = userRepository.findAll().stream()
                .filter(u -> u.getRole().getName() == RoleName.ROLE_ADMIN || 
                             u.getRole().getName() == RoleName.ROLE_DEPOT_MANAGER)
                .collect(Collectors.toList());

        for (MaintenanceRecord record : upcoming) {
            String msg = "MAINTENANCE ALERTS: Bus " + record.getBus().getBusNumber() + " is scheduled for " + 
                         record.getServiceType() + " service on " + record.getServiceDate() + ". Details: " + record.getDescription();
            
            // Check if notification already exists to prevent duplicate spamming
            boolean exists = notificationRepository.findByUserIdOrderByCreatedAtDesc(staffToNotify.get(0).getId()).stream()
                    .anyMatch(n -> n.getMessage().equals(msg));

            if (!exists) {
                for (User staff : staffToNotify) {
                    Notification notification = new Notification();
                    notification.setUser(staff);
                    notification.setMessage(msg);
                    notification.setType(NotificationType.MAINTENANCE);
                    notificationRepository.save(notification);
                }
            }
        }
    }
}
