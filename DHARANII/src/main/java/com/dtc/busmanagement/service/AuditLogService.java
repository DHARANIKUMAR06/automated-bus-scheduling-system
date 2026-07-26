package com.dtc.busmanagement.service;

import com.dtc.busmanagement.entity.AuditLog;
import java.util.List;

public interface AuditLogService {
    List<AuditLog> getAllLogs();
    void logAction(String username, String action, String details, String ipAddress);
}
