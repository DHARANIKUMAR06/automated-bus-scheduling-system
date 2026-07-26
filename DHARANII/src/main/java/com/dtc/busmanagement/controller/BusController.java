package com.dtc.busmanagement.controller;

import com.dtc.busmanagement.dto.BusDTO;
import com.dtc.busmanagement.service.BusService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/buses")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BusController {

    @Autowired
    private BusService busService;

    @GetMapping
    public ResponseEntity<List<BusDTO>> getAllBuses(@RequestParam(value = "search", required = false) String search) {
        List<BusDTO> list = busService.getAllBuses(search);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusDTO> getBusById(@PathVariable Long id) {
        BusDTO dto = busService.getBusById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPOT_MANAGER')")
    public ResponseEntity<BusDTO> createBus(@Valid @RequestBody BusDTO busDTO) {
        BusDTO saved = busService.createBus(busDTO);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPOT_MANAGER')")
    public ResponseEntity<BusDTO> updateBus(@PathVariable Long id, @Valid @RequestBody BusDTO busDTO) {
        BusDTO updated = busService.updateBus(id, busDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBus(@PathVariable Long id) {
        busService.deleteBus(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Bus deleted successfully!");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BusDTO>> getBusesByStatus(@PathVariable String status) {
        List<BusDTO> list = busService.getBusesByStatus(status);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/depot/{depotId}")
    public ResponseEntity<List<BusDTO>> getBusesByDepot(@PathVariable Long depotId) {
        List<BusDTO> list = busService.getBusesByDepot(depotId);
        return ResponseEntity.ok(list);
    }
}
