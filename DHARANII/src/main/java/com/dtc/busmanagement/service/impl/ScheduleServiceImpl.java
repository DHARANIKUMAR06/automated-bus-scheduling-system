package com.dtc.busmanagement.service.impl;

import com.dtc.busmanagement.dto.ScheduleDTO;
import com.dtc.busmanagement.entity.*;
import com.dtc.busmanagement.enums.BusStatus;
import com.dtc.busmanagement.enums.ConductorStatus;
import com.dtc.busmanagement.enums.DriverStatus;
import com.dtc.busmanagement.enums.ShiftType;
import com.dtc.busmanagement.enums.TripStatus;
import com.dtc.busmanagement.exception.BadRequestException;
import com.dtc.busmanagement.exception.ResourceNotFoundException;
import com.dtc.busmanagement.repository.*;
import com.dtc.busmanagement.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private ConductorRepository conductorRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private String getCurrentUsername() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return "system";
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private ScheduleDTO convertToDTO(Schedule s) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(s.getId());
        dto.setRouteId(s.getRoute().getId());
        dto.setRouteNumber(s.getRoute().getRouteNumber());
        dto.setStartLocation(s.getRoute().getStartLocation());
        dto.setEndLocation(s.getRoute().getEndLocation());
        dto.setBusId(s.getBus().getId());
        dto.setBusNumber(s.getBus().getBusNumber());
        dto.setDriverId(s.getDriver().getId());
        dto.setDriverName(s.getDriver().getUser().getFullName());
        dto.setConductorId(s.getConductor().getId());
        dto.setConductorName(s.getConductor().getUser().getFullName());
        dto.setDepartureTime(s.getDepartureTime());
        dto.setArrivalTime(s.getArrivalTime());
        dto.setShiftType(s.getShiftType().name());
        dto.setIsPeakHour(s.getIsPeakHour());
        dto.setScheduleDate(s.getScheduleDate());
        dto.setStatus(s.getStatus());
        return dto;
    }

    @Override
    public List<ScheduleDTO> getAllSchedules(LocalDate date) {
        List<Schedule> list;
        if (date != null) {
            list = scheduleRepository.findByScheduleDate(date);
        } else {
            list = scheduleRepository.findAll();
        }
        return list.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public ScheduleDTO getScheduleById(Long id) {
        Schedule s = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
        return convertToDTO(s);
    }

    @Override
    @Transactional
    public ScheduleDTO createSchedule(ScheduleDTO dto) {
        // Validate conflicts before creation
        validateScheduleConflicts(dto.getBusId(), dto.getDriverId(), dto.getConductorId(),
                dto.getScheduleDate(), dto.getDepartureTime(), dto.getArrivalTime(), null);

        Route route = routeRepository.findById(dto.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));
        Bus bus = busRepository.findById(dto.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));
        Driver driver = driverRepository.findById(dto.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
        Conductor conductor = conductorRepository.findById(dto.getConductorId())
                .orElseThrow(() -> new ResourceNotFoundException("Conductor not found"));

        Schedule schedule = new Schedule();
        schedule.setRoute(route);
        schedule.setBus(bus);
        schedule.setDriver(driver);
        schedule.setConductor(conductor);
        schedule.setDepartureTime(dto.getDepartureTime());
        schedule.setArrivalTime(dto.getArrivalTime());
        schedule.setShiftType(ShiftType.valueOf(dto.getShiftType().toUpperCase()));
        schedule.setIsPeakHour(dto.getIsPeakHour());
        schedule.setScheduleDate(dto.getScheduleDate());
        schedule.setStatus("ACTIVE");

        Schedule savedSchedule = scheduleRepository.save(schedule);

        // Auto-create a corresponding TRIP for operation control
        Trip trip = new Trip();
        trip.setSchedule(savedSchedule);
        trip.setTripDate(dto.getScheduleDate());
        trip.setStatus(TripStatus.SCHEDULED);
        trip.setCurrentStop(route.getStartLocation());
        tripRepository.save(trip);

        // Audit Log
        AuditLog log = new AuditLog(getCurrentUsername(), "SCHEDULE_CREATE", 
                "Manually scheduled Route " + route.getRouteNumber() + " with Bus " + bus.getBusNumber() + " for " + dto.getScheduleDate(), 
                "127.0.0.1");
        auditLogRepository.save(log);

        return convertToDTO(savedSchedule);
    }

    @Override
    @Transactional
    public ScheduleDTO updateSchedule(Long id, ScheduleDTO dto) {
        Schedule s = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));

        // Validate conflicts including self-check bypass
        validateScheduleConflicts(dto.getBusId(), dto.getDriverId(), dto.getConductorId(),
                dto.getScheduleDate(), dto.getDepartureTime(), dto.getArrivalTime(), id);

        Route route = routeRepository.findById(dto.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));
        Bus bus = busRepository.findById(dto.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found"));
        Driver driver = driverRepository.findById(dto.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
        Conductor conductor = conductorRepository.findById(dto.getConductorId())
                .orElseThrow(() -> new ResourceNotFoundException("Conductor not found"));

        s.setRoute(route);
        s.setBus(bus);
        s.setDriver(driver);
        s.setConductor(conductor);
        s.setDepartureTime(dto.getDepartureTime());
        s.setArrivalTime(dto.getArrivalTime());
        s.setShiftType(ShiftType.valueOf(dto.getShiftType().toUpperCase()));
        s.setIsPeakHour(dto.getIsPeakHour());
        s.setScheduleDate(dto.getScheduleDate());
        s.setStatus(dto.getStatus());

        Schedule updated = scheduleRepository.save(s);

        // Audit Log
        AuditLog log = new AuditLog(getCurrentUsername(), "SCHEDULE_UPDATE", 
                "Updated schedule ID " + id + " for Route " + route.getRouteNumber(), 
                "127.0.0.1");
        auditLogRepository.save(log);

        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long id) {
        Schedule s = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
        
        // Also delete associated trips to maintain referential integrity
        // Normally we might set status to CANCELLED instead of hard deleting, which is safer
        s.setStatus("CANCELLED");
        scheduleRepository.save(s);

        // Audit Log
        AuditLog log = new AuditLog(getCurrentUsername(), "SCHEDULE_CANCEL", "Cancelled schedule ID " + id, "127.0.0.1");
        auditLogRepository.save(log);
    }

    @Override
    @Transactional
    public List<ScheduleDTO> autoGenerateSchedules(LocalDate date, Long routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + routeId));

        int durationMins = route.getEstimatedDurationMins();
        LocalTime startTime = LocalTime.of(6, 0); // System starts operations at 6:00 AM
        LocalTime endTime = LocalTime.of(22, 0);  // Ends at 10:00 PM (departure cutoff)

        // Query available resources
        List<Bus> activeBuses = busRepository.findByStatus(BusStatus.ACTIVE);
        List<Driver> activeDrivers = driverRepository.findByStatus(DriverStatus.AVAILABLE).stream()
                .filter(d -> d.getLicenseExpiryDate().isAfter(date)) // Check license expiry
                .collect(Collectors.toList());
        List<Conductor> activeConductors = conductorRepository.findByStatus(ConductorStatus.AVAILABLE);

        if (activeBuses.isEmpty() || activeDrivers.isEmpty() || activeConductors.isEmpty()) {
            throw new BadRequestException("Insufficient active resources (Buses, Drivers, or Conductors) available to run scheduling.");
        }

        List<Schedule> generatedSchedules = new ArrayList<>();
        LocalTime currentTime = startTime;

        while (currentTime.isBefore(endTime)) {
            // Determine if peak hour: 8:00 - 11:00 or 17:00 - 20:00
            boolean isPeak = (currentTime.isAfter(LocalTime.of(7, 59)) && currentTime.isBefore(LocalTime.of(11, 1))) ||
                             (currentTime.isAfter(LocalTime.of(16, 59)) && currentTime.isBefore(LocalTime.of(20, 1)));

            int headway = isPeak ? 20 : 40; // 20 mins peak, 40 mins off-peak
            LocalTime departure = currentTime;
            LocalTime arrival = departure.plusMinutes(durationMins);

            // Determine Shift Type
            ShiftType shiftType = ShiftType.MORNING;
            if (departure.isAfter(LocalTime.of(14, 0)) && departure.isBefore(LocalTime.of(22, 0))) {
                shiftType = ShiftType.EVENING;
            } else if (departure.isAfter(LocalTime.of(21, 59)) || departure.isBefore(LocalTime.of(6, 0))) {
                shiftType = ShiftType.NIGHT;
            }

            // Find a conflict-free Bus, Driver, Conductor
            Bus selectedBus = null;
            for (Bus bus : activeBuses) {
                List<Schedule> conflicts = scheduleRepository.findOverlappingBusSchedules(bus.getId(), date, departure, arrival);
                if (conflicts.isEmpty()) {
                    selectedBus = bus;
                    break;
                }
            }

            Driver selectedDriver = null;
            for (Driver driver : activeDrivers) {
                // Check overlap
                List<Schedule> conflicts = scheduleRepository.findOverlappingDriverSchedules(driver.getId(), date, departure, arrival);
                if (!conflicts.isEmpty()) {
                    continue;
                }

                // Check shift hours (limit to 8 hours daily)
                List<Schedule> existingSchedules = scheduleRepository.findByDriverIdAndScheduleDate(driver.getId(), date);
                long currentMins = existingSchedules.stream()
                        .mapToLong(s -> Duration.between(s.getDepartureTime(), s.getArrivalTime()).toMinutes())
                        .sum();

                if (currentMins + durationMins <= 480) { // 8 hours * 60 = 480 minutes max
                    selectedDriver = driver;
                    break;
                }
            }

            Conductor selectedConductor = null;
            for (Conductor conductor : activeConductors) {
                List<Schedule> conflicts = scheduleRepository.findOverlappingConductorSchedules(conductor.getId(), date, departure, arrival);
                if (conflicts.isEmpty()) {
                    selectedConductor = conductor;
                    break;
                }
            }

            // If we successfully matched all three resources, save the schedule
            if (selectedBus != null && selectedDriver != null && selectedConductor != null) {
                Schedule schedule = new Schedule();
                schedule.setRoute(route);
                schedule.setBus(selectedBus);
                schedule.setDriver(selectedDriver);
                schedule.setConductor(selectedConductor);
                schedule.setDepartureTime(departure);
                schedule.setArrivalTime(arrival);
                schedule.setShiftType(shiftType);
                schedule.setIsPeakHour(isPeak);
                schedule.setScheduleDate(date);
                schedule.setStatus("ACTIVE");

                Schedule saved = scheduleRepository.save(schedule);
                generatedSchedules.add(saved);

                // Auto-create a matching trip for tracking
                Trip trip = new Trip();
                trip.setSchedule(saved);
                trip.setTripDate(date);
                trip.setStatus(TripStatus.SCHEDULED);
                trip.setCurrentStop(route.getStartLocation());
                tripRepository.save(trip);
            }

            currentTime = currentTime.plusMinutes(headway);
        }

        if (generatedSchedules.isEmpty()) {
            throw new BadRequestException("No conflict-free time slots could be generated. Try releasing driver/bus allocations or changing dates.");
        }

        // Audit Log
        AuditLog log = new AuditLog(getCurrentUsername(), "AUTO_SCHEDULE_GENERATE", 
                "Auto-generated " + generatedSchedules.size() + " schedules for Route " + route.getRouteNumber() + " on " + date, 
                "127.0.0.1");
        auditLogRepository.save(log);

        return generatedSchedules.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private void validateScheduleConflicts(Long busId, Long driverId, Long conductorId,
                                           LocalDate date, LocalTime start, LocalTime end, Long selfScheduleId) {
        
        // 1. Bus overlapping schedule validation
        List<Schedule> busConflicts = scheduleRepository.findOverlappingBusSchedules(busId, date, start, end);
        if (selfScheduleId != null) {
            busConflicts = busConflicts.stream().filter(s -> !s.getId().equals(selfScheduleId)).collect(Collectors.toList());
        }
        if (!busConflicts.isEmpty()) {
            throw new BadRequestException("Conflict Detected: Bus is already scheduled on a trip between " + 
                    busConflicts.get(0).getDepartureTime() + " and " + busConflicts.get(0).getArrivalTime());
        }

        // 2. Driver overlapping schedule validation
        List<Schedule> driverConflicts = scheduleRepository.findOverlappingDriverSchedules(driverId, date, start, end);
        if (selfScheduleId != null) {
            driverConflicts = driverConflicts.stream().filter(s -> !s.getId().equals(selfScheduleId)).collect(Collectors.toList());
        }
        if (!driverConflicts.isEmpty()) {
            throw new BadRequestException("Conflict Detected: Driver is already assigned to a route between " + 
                    driverConflicts.get(0).getDepartureTime() + " and " + driverConflicts.get(0).getArrivalTime());
        }

        // 3. Conductor overlapping schedule validation
        List<Schedule> conductorConflicts = scheduleRepository.findOverlappingConductorSchedules(conductorId, date, start, end);
        if (selfScheduleId != null) {
            conductorConflicts = conductorConflicts.stream().filter(s -> !s.getId().equals(selfScheduleId)).collect(Collectors.toList());
        }
        if (!conductorConflicts.isEmpty()) {
            throw new BadRequestException("Conflict Detected: Conductor is already assigned to a route between " + 
                    conductorConflicts.get(0).getDepartureTime() + " and " + conductorConflicts.get(0).getArrivalTime());
        }

        // 4. Driver License Validity Check
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
        if (driver.getLicenseExpiryDate().isBefore(date)) {
            throw new BadRequestException("Driver License Validation Error: Driver's license has expired as of " + date);
        }

        // 5. Driver Daily Shift Limit Check (Max 8 hours total duration)
        long durationProposed = Duration.between(start, end).toMinutes();
        List<Schedule> existingDriverSchedules = scheduleRepository.findByDriverIdAndScheduleDate(driverId, date);
        if (selfScheduleId != null) {
            existingDriverSchedules = existingDriverSchedules.stream().filter(s -> !s.getId().equals(selfScheduleId)).collect(Collectors.toList());
        }
        long totalMins = existingDriverSchedules.stream()
                .mapToLong(s -> Duration.between(s.getDepartureTime(), s.getArrivalTime()).toMinutes())
                .sum();
        if (totalMins + durationProposed > 480) { // 8 hours limit
            throw new BadRequestException("Driver Shift Validation: Assigned driver would exceed the 8-hour maximum shift limit (Current scheduled time: " + totalMins + " mins).");
        }
    }
}
