package com.dtc.busmanagement.service;

import com.dtc.busmanagement.dto.MaintenanceDTO;
import java.util.List;

public interface MaintenanceService {
    List<MaintenanceDTO> getAllMaintenance();
    MaintenanceDTO getMaintenanceById(Long id);
    MaintenanceDTO createMaintenance(MaintenanceDTO maintenanceDTO);
    MaintenanceDTO updateMaintenance(Long id, MaintenanceDTO maintenanceDTO);
    void deleteMaintenance(Long id);
    List<MaintenanceDTO> getMaintenanceByBus(Long busId);
    List<MaintenanceDTO> getUpcomingMaintenance();
    void triggerMaintenanceScheduler();
}
