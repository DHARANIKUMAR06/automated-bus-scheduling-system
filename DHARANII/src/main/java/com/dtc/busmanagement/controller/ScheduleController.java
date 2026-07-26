package com.dtc.busmanagement.controller;

import com.dtc.busmanagement.dto.ScheduleDTO;
import com.dtc.busmanagement.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedules")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<List<ScheduleDTO>> getAllSchedules(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ScheduleDTO> list = scheduleService.getAllSchedules(date);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleDTO> getScheduleById(@PathVariable Long id) {
        ScheduleDTO dto = scheduleService.getScheduleById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER')")
    public ResponseEntity<ScheduleDTO> createSchedule(@Valid @RequestBody ScheduleDTO dto) {
        ScheduleDTO saved = scheduleService.createSchedule(dto);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER')")
    public ResponseEntity<ScheduleDTO> updateSchedule(@PathVariable Long id, @Valid @RequestBody ScheduleDTO dto) {
        ScheduleDTO updated = scheduleService.updateSchedule(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Schedule deleted or cancelled successfully!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auto-generate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCHEDULER')")
    public ResponseEntity<List<ScheduleDTO>> autoGenerateSchedules(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("routeId") Long routeId) {
        List<ScheduleDTO> generated = scheduleService.autoGenerateSchedules(date, routeId);
        return ResponseEntity.ok(generated);
    }
}
