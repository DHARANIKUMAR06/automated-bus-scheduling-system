package com.dtc.busmanagement.service;

import com.dtc.busmanagement.dto.ScheduleDTO;
import com.dtc.busmanagement.entity.*;
import com.dtc.busmanagement.exception.BadRequestException;
import com.dtc.busmanagement.repository.*;
import com.dtc.busmanagement.service.impl.ScheduleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unused")
public class ScheduleServiceTest {

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private BusRepository busRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private ConductorRepository conductorRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private TripRepository tripRepository;

    private ScheduleDTO sampleDto;
    private Bus bus;
    private Driver driver;
    private Conductor conductor;
    private Route route;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock security context
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("test_scheduler");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        sampleDto = new ScheduleDTO();
        sampleDto.setRouteId(1L);
        sampleDto.setBusId(1L);
        sampleDto.setDriverId(1L);
        sampleDto.setConductorId(1L);
        sampleDto.setScheduleDate(LocalDate.now());
        sampleDto.setDepartureTime(LocalTime.of(8, 0));
        sampleDto.setArrivalTime(LocalTime.of(9, 30));
        sampleDto.setShiftType("MORNING");
        sampleDto.setIsPeakHour(true);

        bus = new Bus();
        bus.setId(1L);
        bus.setBusNumber("DL-1PC-1234");

        User driverUser = new User();
        driverUser.setFullName("Sukhvinder Singh");
        driver = new Driver();
        driver.setId(1L);
        driver.setUser(driverUser);
        driver.setLicenseExpiryDate(LocalDate.now().plusYears(1));

        User condUser = new User();
        condUser.setFullName("Ramesh Conductor");
        conductor = new Conductor();
        conductor.setId(1L);
        conductor.setUser(condUser);

        route = new Route();
        route.setId(1L);
        route.setRouteNumber("419");
    }

    @Test
    void testCreateSchedule_DriverOverlapConflict_ThrowsBadRequestException() {
        // Arrange
        when(scheduleRepository.findOverlappingBusSchedules(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList()); // No bus overlap

        // Driver overlap exists
        List<Schedule> driverConflicts = new ArrayList<>();
        Schedule conflictingSchedule = new Schedule();
        conflictingSchedule.setDepartureTime(LocalTime.of(7, 30));
        conflictingSchedule.setArrivalTime(LocalTime.of(9, 0));
        driverConflicts.add(conflictingSchedule);

        when(scheduleRepository.findOverlappingDriverSchedules(any(), any(), any(), any()))
                .thenReturn(driverConflicts);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            scheduleService.createSchedule(sampleDto);
        });
        assertNotNull(exception);

        verify(scheduleRepository, never()).save(any(Schedule.class));
    }

    @Test
    void testCreateSchedule_DriverShiftHoursExceeded_ThrowsBadRequestException() {
        // Arrange
        when(scheduleRepository.findOverlappingBusSchedules(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(scheduleRepository.findOverlappingDriverSchedules(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(scheduleRepository.findOverlappingConductorSchedules(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
        
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));

        // Let's mock existing driver schedules for the date that sum up to 7.5 hours
        List<Schedule> existingSchedules = new ArrayList<>();
        Schedule s1 = new Schedule();
        s1.setDepartureTime(LocalTime.of(10, 0));
        s1.setArrivalTime(LocalTime.of(17, 30)); // 7.5 hours (450 minutes)
        existingSchedules.add(s1);

        when(scheduleRepository.findByDriverIdAndScheduleDate(any(), any())).thenReturn(existingSchedules);

        // Proposed schedule duration is 1.5 hours (90 minutes).
        // Total would be 450 + 90 = 540 minutes (9 hours) which exceeds the 8-hour daily limit (480 minutes).

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            scheduleService.createSchedule(sampleDto);
        });
        assertNotNull(exception);

        verify(scheduleRepository, never()).save(any(Schedule.class));
    }
}
