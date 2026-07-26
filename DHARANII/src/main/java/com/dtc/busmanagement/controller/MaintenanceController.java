package com.dtc.busmanagement.controller;

import com.dtc.busmanagement.dto.MaintenanceDTO;
import com.dtc.busmanagement.service.MaintenanceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MaintenanceController {

    @Autowired
    private MaintenanceService maintenanceService;

    @GetMapping
    public ResponseEntity<List<MaintenanceDTO>> getAllMaintenance() {
        List<MaintenanceDTO> list = maintenanceService.getAllMaintenance();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceDTO> getMaintenanceById(@PathVariable Long id) {
        MaintenanceDTO dto = maintenanceService.getMaintenanceById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPOT_MANAGER')")
    public ResponseEntity<MaintenanceDTO> createMaintenance(@Valid @RequestBody MaintenanceDTO dto) {
        MaintenanceDTO saved = maintenanceService.createMaintenance(dto);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPOT_MANAGER')")
    public ResponseEntity<MaintenanceDTO> updateMaintenance(@PathVariable Long id, @Valid @RequestBody MaintenanceDTO dto) {
        MaintenanceDTO updated = maintenanceService.updateMaintenance(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteMaintenance(@PathVariable Long id) {
        maintenanceService.deleteMaintenance(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Maintenance record deleted successfully!");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<MaintenanceDTO>> getUpcomingMaintenance() {
        List<MaintenanceDTO> list = maintenanceService.getUpcomingMaintenance();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/bus/{busId}")
    public ResponseEntity<List<MaintenanceDTO>> getMaintenanceByBus(@PathVariable Long busId) {
        List<MaintenanceDTO> list = maintenanceService.getMaintenanceByBus(busId);
        return ResponseEntity.ok(list);
    }
}
