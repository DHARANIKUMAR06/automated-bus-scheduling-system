package com.dtc.busmanagement.repository;

import com.dtc.busmanagement.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByScheduleDate(LocalDate date);
    List<Schedule> findByRouteIdAndScheduleDate(Long routeId, LocalDate date);
    List<Schedule> findByDriverIdAndScheduleDate(Long driverId, LocalDate date);
    List<Schedule> findByBusIdAndScheduleDate(Long busId, LocalDate date);
    List<Schedule> findByConductorIdAndScheduleDate(Long conductorId, LocalDate date);

    @Query("SELECT s FROM Schedule s WHERE s.bus.id = :busId AND s.scheduleDate = :date AND s.status = 'ACTIVE' AND " +
           "((s.departureTime <= :endTime AND s.arrivalTime >= :startTime))")
    List<Schedule> findOverlappingBusSchedules(@Param("busId") Long busId,
                                               @Param("date") LocalDate date,
                                               @Param("startTime") LocalTime startTime,
                                               @Param("endTime") LocalTime endTime);

    @Query("SELECT s FROM Schedule s WHERE s.driver.id = :driverId AND s.scheduleDate = :date AND s.status = 'ACTIVE' AND " +
           "((s.departureTime <= :endTime AND s.arrivalTime >= :startTime))")
    List<Schedule> findOverlappingDriverSchedules(@Param("driverId") Long driverId,
                                                  @Param("date") LocalDate date,
                                                  @Param("startTime") LocalTime startTime,
                                                  @Param("endTime") LocalTime endTime);

    @Query("SELECT s FROM Schedule s WHERE s.conductor.id = :conductorId AND s.scheduleDate = :date AND s.status = 'ACTIVE' AND " +
           "((s.departureTime <= :endTime AND s.arrivalTime >= :startTime))")
    List<Schedule> findOverlappingConductorSchedules(@Param("conductorId") Long conductorId,
                                                     @Param("date") LocalDate date,
                                                     @Param("startTime") LocalTime startTime,
                                                     @Param("endTime") LocalTime endTime);
}
