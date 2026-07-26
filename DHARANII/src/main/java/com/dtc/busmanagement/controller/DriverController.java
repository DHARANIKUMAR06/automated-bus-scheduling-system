package com.dtc.busmanagement.controller;

import com.dtc.busmanagement.dto.DriverDTO;
import com.dtc.busmanagement.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DriverController {

    @Autowired
    private DriverService driverService;

    @GetMapping
    public ResponseEntity<List<DriverDTO>> getAllDrivers(@RequestParam(value = "search", required = false) String search) {
        List<DriverDTO> list = driverService.getAllDrivers(search);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverDTO> getDriverById(@PathVariable Long id) {
        DriverDTO dto = driverService.getDriverById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPOT_MANAGER')")
    public ResponseEntity<DriverDTO> createDriver(@Valid @RequestBody DriverDTO driverDTO) {
        DriverDTO saved = driverService.createDriver(driverDTO);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPOT_MANAGER')")
    public ResponseEntity<DriverDTO> updateDriver(@PathVariable Long id, @Valid @RequestBody DriverDTO driverDTO) {
        DriverDTO updated = driverService.updateDriver(id, driverDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteDriver(@PathVariable Long id) {
        driverService.deleteDriver(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Driver deleted successfully!");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DriverDTO>> getDriversByStatus(@PathVariable String status) {
        List<DriverDTO> list = driverService.getDriversByStatus(status);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/depot/{depotId}")
    public ResponseEntity<List<DriverDTO>> getDriversByDepot(@PathVariable Long depotId) {
        List<DriverDTO> list = driverService.getDriversByDepot(depotId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/license-expiring")
    public ResponseEntity<List<DriverDTO>> getLicenseExpiringDrivers() {
        List<DriverDTO> list = driverService.getLicenseExpiringDrivers();
        return ResponseEntity.ok(list);
    }
}
