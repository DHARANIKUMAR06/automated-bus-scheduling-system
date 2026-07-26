package com.dtc.busmanagement.config;

import com.dtc.busmanagement.entity.*;
import com.dtc.busmanagement.enums.*;
import com.dtc.busmanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DepotRepository depotRepository;
    @Autowired private BusRepository busRepository;
    @Autowired private DriverRepository driverRepository;
    @Autowired private ConductorRepository conductorRepository;
    @Autowired private RouteRepository routeRepository;
    @Autowired private StopRepository stopRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private TripRepository tripRepository;
    @Autowired private MaintenanceRecordRepository maintenanceRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Skip if data already exists
        if (roleRepository.count() > 0) return;

        String hash = passwordEncoder.encode("admin123");

        // 1. Roles
        Role adminRole    = roleRepository.save(new Role(RoleName.ROLE_ADMIN));
        Role managerRole  = roleRepository.save(new Role(RoleName.ROLE_DEPOT_MANAGER));
        Role schedRole    = roleRepository.save(new Role(RoleName.ROLE_SCHEDULER));
        Role driverRole   = roleRepository.save(new Role(RoleName.ROLE_DRIVER));
        Role conductorRole= roleRepository.save(new Role(RoleName.ROLE_CONDUCTOR));

        // 2. Depots
        Depot depot1 = new Depot(); depot1.setName("Rajghat Depot"); depot1.setLocation("Rajghat, New Delhi"); depot1.setCapacity(50);
        depot1 = depotRepository.save(depot1);
        Depot depot2 = new Depot(); depot2.setName("Rewari Depot"); depot2.setLocation("Rewari, Haryana"); depot2.setCapacity(40);
        depot2 = depotRepository.save(depot2);
        Depot depot3 = new Depot(); depot3.setName("Mehrauli Depot"); depot3.setLocation("Mehrauli, New Delhi"); depot3.setCapacity(35);
        depot3 = depotRepository.save(depot3);

        // 3. Users
        User dharani = new User(); dharani.setUsername("DHARANIKUMAR"); dharani.setEmail("dharanikumar@dtc.delhi.gov.in");
        dharani.setPassword(passwordEncoder.encode("dharanikumar")); dharani.setRole(adminRole); dharani.setFullName("Dharani Kumar (Administrator)");
        dharani.setPhone("9876543299"); dharani.setStatus("ACTIVE");
        userRepository.save(dharani);

        User admin = new User(); admin.setUsername("admin"); admin.setEmail("admin@dtc.delhi.gov.in");
        admin.setPassword(hash); admin.setRole(adminRole); admin.setFullName("DTC System Administrator");
        admin.setPhone("9876543210"); admin.setStatus("ACTIVE");
        userRepository.save(admin);

        User manager = new User(); manager.setUsername("manager"); manager.setEmail("manager@dtc.delhi.gov.in");
        manager.setPassword(hash); manager.setRole(managerRole); manager.setFullName("Rajesh Kumar (Depot Manager)");
        manager.setPhone("9876543211"); manager.setStatus("ACTIVE");
        userRepository.save(manager);

        User scheduler = new User(); scheduler.setUsername("scheduler"); scheduler.setEmail("scheduler@dtc.delhi.gov.in");
        scheduler.setPassword(hash); scheduler.setRole(schedRole); scheduler.setFullName("Anil Sharma (Chief Scheduler)");
        scheduler.setPhone("9876543212"); scheduler.setStatus("ACTIVE");
        userRepository.save(scheduler);

        User driverUser1 = new User(); driverUser1.setUsername("driver1"); driverUser1.setEmail("driver1@dtc.delhi.gov.in");
        driverUser1.setPassword(hash); driverUser1.setRole(driverRole); driverUser1.setFullName("Sukhvinder Singh (Driver)");
        driverUser1.setPhone("9876543213"); driverUser1.setStatus("ACTIVE");
        driverUser1 = userRepository.save(driverUser1);

        User driverUser2 = new User(); driverUser2.setUsername("driver2"); driverUser2.setEmail("driver2@dtc.delhi.gov.in");
        driverUser2.setPassword(hash); driverUser2.setRole(driverRole); driverUser2.setFullName("Mohammad Riyaz (Driver)");
        driverUser2.setPhone("9876543214"); driverUser2.setStatus("ACTIVE");
        driverUser2 = userRepository.save(driverUser2);

        User condUser1 = new User(); condUser1.setUsername("conductor1"); condUser1.setEmail("conductor1@dtc.delhi.gov.in");
        condUser1.setPassword(hash); condUser1.setRole(conductorRole); condUser1.setFullName("Ramesh Chand (Conductor)");
        condUser1.setPhone("9876543215"); condUser1.setStatus("ACTIVE");
        condUser1 = userRepository.save(condUser1);

        User condUser2 = new User(); condUser2.setUsername("conductor2"); condUser2.setEmail("conductor2@dtc.delhi.gov.in");
        condUser2.setPassword(hash); condUser2.setRole(conductorRole); condUser2.setFullName("Suresh Lal (Conductor)");
        condUser2.setPhone("9876543216"); condUser2.setStatus("ACTIVE");
        condUser2 = userRepository.save(condUser2);

        // 4. Buses
        Bus bus1 = new Bus(); bus1.setBusNumber("DL-1PD-2034"); bus1.setModel("Tata Starbus Hybrid"); bus1.setCapacity(45);
        bus1.setFuelType(FuelType.CNG); bus1.setStatus(BusStatus.ACTIVE); bus1.setDepot(depot1); bus1.setMileageKm(12450.5); bus1.setLastServiceDate(LocalDate.of(2026,6,10));
        bus1 = busRepository.save(bus1);
        Bus bus2 = new Bus(); bus2.setBusNumber("DL-1PD-4412"); bus2.setModel("JBM Ecolife Electric"); bus2.setCapacity(35);
        bus2.setFuelType(FuelType.ELECTRIC); bus2.setStatus(BusStatus.ACTIVE); bus2.setDepot(depot1); bus2.setMileageKm(8500.2); bus2.setLastServiceDate(LocalDate.of(2026,6,15));
        bus2 = busRepository.save(bus2);
        Bus bus3 = new Bus(); bus3.setBusNumber("DL-1PD-8876"); bus3.setModel("Tata LPO 1613"); bus3.setCapacity(50);
        bus3.setFuelType(FuelType.CNG); bus3.setStatus(BusStatus.ACTIVE); bus3.setDepot(depot2); bus3.setMileageKm(45200.0); bus3.setLastServiceDate(LocalDate.of(2026,5,20));
        busRepository.save(bus3);
        Bus bus4 = new Bus(); bus4.setBusNumber("DL-1PD-9901"); bus4.setModel("Ashok Leyland JanBus"); bus4.setCapacity(40);
        bus4.setFuelType(FuelType.DIESEL); bus4.setStatus(BusStatus.MAINTENANCE); bus4.setDepot(depot2); bus4.setMileageKm(62100.8); bus4.setLastServiceDate(LocalDate.of(2026,7,10));
        bus4 = busRepository.save(bus4);

        // 5. Drivers
        Driver driver1 = new Driver(); driver1.setUser(driverUser1); driver1.setLicenseNumber("DL-1420100084321");
        driver1.setLicenseExpiryDate(LocalDate.of(2028,12,15)); driver1.setExperienceYears(12);
        driver1.setStatus(DriverStatus.AVAILABLE); driver1.setDepot(depot1); driver1.setAssignedBus(bus1);
        driver1 = driverRepository.save(driver1);
        Driver driver2 = new Driver(); driver2.setUser(driverUser2); driver2.setLicenseNumber("DL-1420120095654");
        driver2.setLicenseExpiryDate(LocalDate.of(2026,8,10)); driver2.setExperienceYears(8);
        driver2.setStatus(DriverStatus.AVAILABLE); driver2.setDepot(depot1); driver2.setAssignedBus(bus2);
        driver2 = driverRepository.save(driver2);

        // 6. Conductors
        Conductor conductor1 = new Conductor(); conductor1.setUser(condUser1); conductor1.setEmployeeId("EMP-CON-8841");
        conductor1.setStatus(ConductorStatus.AVAILABLE); conductor1.setDepot(depot1);
        conductor1 = conductorRepository.save(conductor1);
        Conductor conductor2 = new Conductor(); conductor2.setUser(condUser2); conductor2.setEmployeeId("EMP-CON-9023");
        conductor2.setStatus(ConductorStatus.AVAILABLE); conductor2.setDepot(depot1);
        conductor2 = conductorRepository.save(conductor2);

        // 7. Routes
        Route route1 = new Route(); route1.setRouteNumber("502");
        route1.setStartLocation("Mehrauli Terminal"); route1.setEndLocation("Old Delhi Railway Station");
        route1.setDistanceKm(28.5); route1.setEstimatedDurationMins(75); route1.setStatus(RouteStatus.ACTIVE);
        route1 = routeRepository.save(route1);
        Route route2 = new Route(); route2.setRouteNumber("419");
        route2.setStartLocation("Qutub Minar Metro Station"); route2.setEndLocation("ISBT Kashmere Gate");
        route2.setDistanceKm(28.0); route2.setEstimatedDurationMins(90); route2.setStatus(RouteStatus.ACTIVE);
        route2 = routeRepository.save(route2);

        // 8. Stops for Route 1
        stopRepository.save(createStop(route1, "Mehrauli Terminal", 1, 0.0, 0));
        stopRepository.save(createStop(route1, "Chirag Delhi", 2, 3.2, 8));
        stopRepository.save(createStop(route1, "Alaknanda Market", 3, 7.5, 20));
        stopRepository.save(createStop(route1, "Govindpuri Metro Station", 4, 11.0, 30));
        stopRepository.save(createStop(route1, "Ashram Chowk", 5, 16.8, 45));
        stopRepository.save(createStop(route1, "Lajpat Nagar", 6, 20.5, 55));
        stopRepository.save(createStop(route1, "Old Delhi Railway Station", 7, 28.5, 75));

        // 8. Stops for Route 2
        stopRepository.save(createStop(route2, "Qutub Minar Metro Station", 1, 0.0, 0));
        stopRepository.save(createStop(route2, "Adchini", 2, 1.8, 5));
        stopRepository.save(createStop(route2, "AIIMS", 3, 4.5, 12));
        stopRepository.save(createStop(route2, "Connaught Place", 4, 9.2, 25));
        stopRepository.save(createStop(route2, "ISBT Kashmere Gate", 5, 17.8, 50));

        // 9. Schedules
        Schedule sch1 = new Schedule(); sch1.setRoute(route1); sch1.setBus(bus1); sch1.setDriver(driver1);
        sch1.setConductor(conductor1); sch1.setDepartureTime(LocalTime.of(8,0)); sch1.setArrivalTime(LocalTime.of(9,15));
        sch1.setShiftType(ShiftType.MORNING); sch1.setIsPeakHour(true); sch1.setScheduleDate(LocalDate.now()); sch1.setStatus("ACTIVE");
        sch1 = scheduleRepository.save(sch1);
        Schedule sch2 = new Schedule(); sch2.setRoute(route1); sch2.setBus(bus2); sch2.setDriver(driver2);
        sch2.setConductor(conductor2); sch2.setDepartureTime(LocalTime.of(8,30)); sch2.setArrivalTime(LocalTime.of(9,45));
        sch2.setShiftType(ShiftType.MORNING); sch2.setIsPeakHour(true); sch2.setScheduleDate(LocalDate.now()); sch2.setStatus("ACTIVE");
        sch2 = scheduleRepository.save(sch2);

        // 10. Trips
        Trip trip1 = new Trip(); trip1.setSchedule(sch1); trip1.setTripDate(LocalDate.now());
        trip1.setActualDepartureTime(LocalDateTime.now().withHour(8).withMinute(5));
        trip1.setActualArrivalTime(LocalDateTime.now().withHour(9).withMinute(22));
        trip1.setDelayMinutes(7); trip1.setStatus(TripStatus.COMPLETED);
        trip1.setCurrentStop("Old Delhi Railway Station"); trip1.setNotes("Slight traffic at Chirag Delhi");
        tripRepository.save(trip1);
        Trip trip2 = new Trip(); trip2.setSchedule(sch2); trip2.setTripDate(LocalDate.now());
        trip2.setActualDepartureTime(LocalDateTime.now().withHour(8).withMinute(32));
        trip2.setDelayMinutes(15); trip2.setStatus(TripStatus.EN_ROUTE);
        trip2.setCurrentStop("Chirag Delhi"); trip2.setNotes("Delayed due to congestion on Ring Road");
        tripRepository.save(trip2);

        // 11. Maintenance
        MaintenanceRecord mr1 = new MaintenanceRecord(); mr1.setBus(bus1); mr1.setServiceDate(LocalDate.of(2026,6,10));
        mr1.setServiceType("ROUTINE"); mr1.setDescription("Engine oil change, brake fluid top up and air filter cleaning.");
        mr1.setCost(4500.0); mr1.setStatus(MaintenanceStatus.COMPLETED); mr1.setTechnicianNotes("Next service recommended in 5000 km.");
        maintenanceRepository.save(mr1);
        MaintenanceRecord mr2 = new MaintenanceRecord(); mr2.setBus(bus4); mr2.setServiceDate(LocalDate.of(2026,7,10));
        mr2.setServiceType("REPAIR"); mr2.setDescription("AC compressor replacement and coolant leak repair.");
        mr2.setCost(18200.0); mr2.setStatus(MaintenanceStatus.IN_PROGRESS); mr2.setTechnicianNotes("Waiting for JBM spare parts delivery.");
        maintenanceRepository.save(mr2);

        // 12. Notifications
        Notification n1 = new Notification(); n1.setUser(admin); n1.setMessage("Bus DL-1PD-9901 is currently under maintenance. Estimated completion: 2026-07-15.");
        n1.setType(NotificationType.MAINTENANCE); n1.setReadStatus(false);
        notificationRepository.save(n1);
        Notification n2 = new Notification(); n2.setUser(admin); n2.setMessage("Driver Mohammad Riyaz: License expiring on 2026-08-10 (under 30 days). Please arrange for renewal.");
        n2.setType(NotificationType.LICENSE_EXPIRY); n2.setReadStatus(false);
        notificationRepository.save(n2);

        // 13. Audit Logs
        AuditLog al1 = new AuditLog("admin", "USER_LOGIN", "Administrator logged in successfully", "127.0.0.1");
        auditLogRepository.save(al1);
        AuditLog al2 = new AuditLog("scheduler", "AUTO_SCHEDULE_GENERATE", "Generated schedule slots for Route 419", "127.0.0.1");
        auditLogRepository.save(al2);

        System.out.println("✅ DTC Data seeded successfully! Login with admin/admin123");
    }

    private Stop createStop(Route route, String name, int seq, double dist, int time) {
        Stop s = new Stop();
        s.setRoute(route);
        s.setStopName(name);
        s.setSequenceNumber(seq);
        s.setDistanceFromStart(dist);
        s.setEstimatedTimeFromStart(time);
        return s;
    }
}
