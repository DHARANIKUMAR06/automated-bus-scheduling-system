package com.dtc.busmanagement.controller;

import com.dtc.busmanagement.dto.DashboardStatsDTO;
import com.dtc.busmanagement.enums.BusStatus;
import com.dtc.busmanagement.enums.FuelType;
import com.dtc.busmanagement.enums.TripStatus;
import com.dtc.busmanagement.repository.*;
import com.dtc.busmanagement.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private ConductorRepository conductorRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        DashboardStatsDTO dto = new DashboardStatsDTO();
        
        dto.setTotalBuses(busRepository.count());
        dto.setActiveBuses(busRepository.countByStatus(BusStatus.ACTIVE));
        dto.setTotalDrivers(driverRepository.count());
        dto.setTotalConductors(conductorRepository.count());
        
        LocalDate today = LocalDate.now();
        dto.setTodayTrips(tripRepository.countByTripDate(today));
        dto.setCompletedTrips(tripRepository.countByTripDateAndStatus(today, TripStatus.COMPLETED));
        dto.setDelayedTrips(tripRepository.countDelayedTrips(today));

        // Get notifications count for logged in user
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            Long userId = userDetails.getId();
            dto.setActiveNotifications(notificationRepository.countByUserIdAndReadStatus(userId, false));
        } else {
            dto.setActiveNotifications(0);
        }

        // Bus Fuel Type Distribution
        Map<String, Long> fuelDist = new HashMap<>();
        for (FuelType fuel : FuelType.values()) {
            long count = busRepository.findAll().stream().filter(b -> b.getFuelType() == fuel).count();
            fuelDist.put(fuel.name(), count);
        }
        dto.setBusFuelTypeDistribution(fuelDist);

        // Bus Status Distribution
        Map<String, Long> statusDist = new HashMap<>();
        for (BusStatus status : BusStatus.values()) {
            statusDist.put(status.name(), busRepository.countByStatus(status));
        }
        dto.setBusStatusDistribution(statusDist);

        // Trip Status Distribution
        Map<String, Long> tripDist = new HashMap<>();
        for (TripStatus status : TripStatus.values()) {
            tripDist.put(status.name(), tripRepository.countByTripDateAndStatus(today, status));
        }
        dto.setTripStatusDistribution(tripDist);

        return ResponseEntity.ok(dto);
    }
}
