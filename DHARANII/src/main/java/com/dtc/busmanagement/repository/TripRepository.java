package com.dtc.busmanagement.repository;

import com.dtc.busmanagement.entity.Trip;
import com.dtc.busmanagement.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByTripDate(LocalDate date);
    List<Trip> findByStatus(TripStatus status);
    List<Trip> findByTripDateAndStatus(LocalDate date, TripStatus status);

    long countByTripDate(LocalDate date);
    long countByTripDateAndStatus(LocalDate date, TripStatus status);

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.tripDate = :date AND t.delayMinutes > 0 AND t.status != 'CANCELLED'")
    long countDelayedTrips(@Param("date") LocalDate date);

    @Query("SELECT t FROM Trip t WHERE t.tripDate = :date AND t.status IN ('SCHEDULED', 'EN_ROUTE', 'DELAYED')")
    List<Trip> findActiveTrips(@Param("date") LocalDate date);
}
