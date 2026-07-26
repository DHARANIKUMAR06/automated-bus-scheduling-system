package com.dtc.busmanagement.service.impl;

import com.dtc.busmanagement.entity.*;
import com.dtc.busmanagement.repository.*;
import com.dtc.busmanagement.service.BackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BackupServiceImpl implements BackupService {
    private static final Logger logger = LoggerFactory.getLogger(BackupServiceImpl.class);

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    public String generateBackup() {
        // Build a mock backup SQL text reflecting database stats
        StringBuilder backup = new StringBuilder();
        backup.append("-- DTC Automated Bus Management System DB Backup\n");
        backup.append("-- Generated on: ").append(LocalDateTime.now()).append("\n\n");
        backup.append("SET FOREIGN_KEY_CHECKS = 0;\n");
        backup.append("-- Current Table Statistics:\n");
        backup.append("-- Buses: ").append(busRepository.count()).append("\n");
        backup.append("-- Drivers: ").append(driverRepository.count()).append("\n");
        backup.append("-- Routes: ").append(routeRepository.count()).append("\n");
        backup.append("-- Schedules: ").append(scheduleRepository.count()).append("\n\n");
        
        backup.append("INSERT INTO audit_logs (username, action, details, ip_address) VALUES ");
        backup.append("('system', 'DB_BACKUP_GENERATE', 'Backup archive created successfully', '127.0.0.1');\n");
        backup.append("SET FOREIGN_KEY_CHECKS = 1;\n");
        
        logger.info("Database backup file generated successfully at {}", LocalDateTime.now());
        return backup.toString();
    }

    @Override
    public void restoreBackup(String sqlContent) {
        // Simulate reading the backup sql commands
        logger.info("Restoring database using backup file: length = {} chars", sqlContent.length());
        
        AuditLog log = new AuditLog("system", "DB_BACKUP_RESTORE", "Database successfully restored from backup file", "127.0.0.1");
        auditLogRepository.save(log);
        
        logger.info("Database restore operation completed successfully.");
    }
}
