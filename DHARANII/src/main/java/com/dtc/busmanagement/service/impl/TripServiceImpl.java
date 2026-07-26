package com.dtc.busmanagement.service.impl;

import com.dtc.busmanagement.dto.TripDTO;
import com.dtc.busmanagement.entity.*;
import com.dtc.busmanagement.enums.*;
import com.dtc.busmanagement.exception.BadRequestException;
import com.dtc.busmanagement.exception.ResourceNotFoundException;
import com.dtc.busmanagement.repository.*;
import com.dtc.busmanagement.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TripServiceImpl implements TripService {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private ConductorRepository conductorRepository;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private String getCurrentUsername() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return "system";
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private TripDTO convertToDTO(Trip t) {
        TripDTO dto = new TripDTO();
        dto.setId(t.getId());
        dto.setScheduleId(t.getSchedule().getId());
        dto.setRouteNumber(t.getSchedule().getRoute().getRouteNumber());
        dto.setStartLocation(t.getSchedule().getRoute().getStartLocation());
        dto.setEndLocation(t.getSchedule().getRoute().getEndLocation());
        dto.setBusNumber(t.getSchedule().getBus().getBusNumber());
        dto.setDriverName(t.getSchedule().getDriver().getUser().getFullName());
        dto.setConductorName(t.getSchedule().getConductor().getUser().getFullName());
        dto.setTripDate(t.getTripDate());
        dto.setActualDepartureTime(t.getActualDepartureTime());
        dto.setActualArrivalTime(t.getActualArrivalTime());
        dto.setDelayMinutes(t.getDelayMinutes());
        dto.setStatus(t.getStatus().name());
        dto.setCurrentStop(t.getCurrentStop());
        dto.setNotes(t.getNotes());
        dto.setScheduledDepartureTime(t.getSchedule().getDepartureTime());
        dto.setScheduledArrivalTime(t.getSchedule().getArrivalTime());
        return dto;
    }

    @Override
    public List<TripDTO> getActiveTrips() {
        return tripRepository.findActiveTrips(LocalDate.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TripDTO> getAllTripsForDate(LocalDate date) {
        LocalDate searchDate = date != null ? date : LocalDate.now();
        return tripRepository.findByTripDate(searchDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TripDTO getTripById(Long id) {
        Trip t = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found with id: " + id));
        return convertToDTO(t);
    }

    @Override
    @Transactional
    public TripDTO startTrip(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (trip.getStatus() != TripStatus.SCHEDULED) {
            throw new BadRequestException("Trip cannot be started from its current status: " + trip.getStatus());
        }

        trip.setStatus(TripStatus.EN_ROUTE);
        trip.setActualDepartureTime(LocalDateTime.now());
        trip.setCurrentStop(trip.getSchedule().getRoute().getStartLocation());
        Trip savedTrip = tripRepository.save(trip);

        // Update driver, conductor, and bus status
        Driver driver = trip.getSchedule().getDriver();
        driver.setStatus(DriverStatus.ON_TRIP);
        driverRepository.save(driver);

        Conductor conductor = trip.getSchedule().getConductor();
        conductor.setStatus(ConductorStatus.ON_TRIP);
        conductorRepository.save(conductor);

        // Log operation
        AuditLog log = new AuditLog(getCurrentUsername(), "TRIP_START", 
                "Started trip ID " + id + " for Route " + trip.getSchedule().getRoute().getRouteNumber(), 
                "127.0.0.1");
        auditLogRepository.save(log);

        return convertToDTO(savedTrip);
    }

    @Override
    @Transactional
    public TripDTO endTrip(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (trip.getStatus() != TripStatus.EN_ROUTE && trip.getStatus() != TripStatus.DELAYED) {
            throw new BadRequestException("Trip cannot be ended from its current status: " + trip.getStatus());
        }

        trip.setStatus(TripStatus.COMPLETED);
        trip.setActualArrivalTime(LocalDateTime.now());
        trip.setCurrentStop(trip.getSchedule().getRoute().getEndLocation());
        Trip savedTrip = tripRepository.save(trip);

        // Revert resources to AVAILABLE
        Driver driver = trip.getSchedule().getDriver();
        driver.setStatus(DriverStatus.AVAILABLE);
        driverRepository.save(driver);

        Conductor conductor = trip.getSchedule().getConductor();
        conductor.setStatus(ConductorStatus.AVAILABLE);
        conductorRepository.save(conductor);

        // Update mileage on the bus
        Bus bus = trip.getSchedule().getBus();
        double tripDistance = trip.getSchedule().getRoute().getDistanceKm();
        bus.setMileageKm(bus.getMileageKm() + tripDistance);
        busRepository.save(bus);

        // Log operation
        AuditLog log = new AuditLog(getCurrentUsername(), "TRIP_END", 
                "Ended trip ID " + id + " for Route " + trip.getSchedule().getRoute().getRouteNumber() + ". Bus mileage updated.", 
                "127.0.0.1");
        auditLogRepository.save(log);

        return convertToDTO(savedTrip);
    }

    @Override
    @Transactional
    public TripDTO updateTripStatusAndDelay(Long id, String status, int delayMinutes, String currentStop, String notes) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        TripStatus newStatus = TripStatus.valueOf(status.toUpperCase());
        trip.setStatus(newStatus);
        trip.setDelayMinutes(delayMinutes);
        trip.setCurrentStop(currentStop);
        trip.setNotes(notes);

        Trip saved = tripRepository.save(trip);

        // Send alert if delayed significantly (e.g. > 10 minutes)
        if (delayMinutes > 10) {
            triggerDelayNotifications(saved);
        }

        return convertToDTO(saved);
    }

    private void triggerDelayNotifications(Trip trip) {
        // Find managers and schedulers to alert
        List<User> dispatchers = userRepository.findAll().stream()
                .filter(u -> u.getRole().getName() == RoleName.ROLE_ADMIN || 
                             u.getRole().getName() == RoleName.ROLE_DEPOT_MANAGER || 
                             u.getRole().getName() == RoleName.ROLE_SCHEDULER)
                .collect(Collectors.toList());

        String message = "DELAY ALERT: Trip #" + trip.getId() + " (Route " + trip.getSchedule().getRoute().getRouteNumber() + 
                         ") is currently delayed by " + trip.getDelayMinutes() + " minutes at stop: " + trip.getCurrentStop() + 
                         ". Driver: " + trip.getSchedule().getDriver().getUser().getFullName();

        for (User dispatcher : dispatchers) {
            Notification notification = new Notification();
            notification.setUser(dispatcher);
            notification.setMessage(message);
            notification.setType(NotificationType.DELAY);
            notificationRepository.save(notification);
        }
    }
}
