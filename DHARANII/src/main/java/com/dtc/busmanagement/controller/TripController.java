package com.dtc.busmanagement.controller;

import com.dtc.busmanagement.dto.TripDTO;
import com.dtc.busmanagement.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TripController {

    @Autowired
    private TripService tripService;

    @GetMapping("/live")
    public ResponseEntity<List<TripDTO>> getLiveTrips() {
        List<TripDTO> active = tripService.getActiveTrips();
        return ResponseEntity.ok(active);
    }

    @GetMapping
    public ResponseEntity<List<TripDTO>> getAllTrips(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TripDTO> list = tripService.getAllTripsForDate(date);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripDTO> getTripById(@PathVariable Long id) {
        TripDTO dto = tripService.getTripById(id);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER') or hasRole('DRIVER') or hasRole('CONDUCTOR')")
    public ResponseEntity<TripDTO> startTrip(@PathVariable Long id) {
        TripDTO dto = tripService.startTrip(id);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/end")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER') or hasRole('DRIVER') or hasRole('CONDUCTOR')")
    public ResponseEntity<TripDTO> endTrip(@PathVariable Long id) {
        TripDTO dto = tripService.endTrip(id);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER') or hasRole('DRIVER') or hasRole('CONDUCTOR')")
    public ResponseEntity<TripDTO> updateTripStatus(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @RequestParam("delayMinutes") int delayMinutes,
            @RequestParam("currentStop") String currentStop,
            @RequestParam(value = "notes", required = false) String notes) {
        
        TripDTO dto = tripService.updateTripStatusAndDelay(id, status, delayMinutes, currentStop, notes);
        return ResponseEntity.ok(dto);
    }
}
