package com.dtc.busmanagement.controller;

import com.dtc.busmanagement.dto.ConductorDTO;
import com.dtc.busmanagement.service.ConductorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conductors")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ConductorController {

    @Autowired
    private ConductorService conductorService;

    @GetMapping
    public ResponseEntity<List<ConductorDTO>> getAllConductors(@RequestParam(value = "search", required = false) String search) {
        List<ConductorDTO> list = conductorService.getAllConductors(search);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConductorDTO> getConductorById(@PathVariable Long id) {
        ConductorDTO dto = conductorService.getConductorById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPOT_MANAGER')")
    public ResponseEntity<ConductorDTO> createConductor(@Valid @RequestBody ConductorDTO conductorDTO) {
        ConductorDTO saved = conductorService.createConductor(conductorDTO);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEPOT_MANAGER')")
    public ResponseEntity<ConductorDTO> updateConductor(@PathVariable Long id, @Valid @RequestBody ConductorDTO conductorDTO) {
        ConductorDTO updated = conductorService.updateConductor(id, conductorDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteConductor(@PathVariable Long id) {
        conductorService.deleteConductor(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Conductor deleted successfully!");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ConductorDTO>> getConductorsByStatus(@PathVariable String status) {
        List<ConductorDTO> list = conductorService.getConductorsByStatus(status);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/depot/{depotId}")
    public ResponseEntity<List<ConductorDTO>> getConductorsByDepot(@PathVariable Long depotId) {
        List<ConductorDTO> list = conductorService.getConductorsByDepot(depotId);
        return ResponseEntity.ok(list);
    }
}
