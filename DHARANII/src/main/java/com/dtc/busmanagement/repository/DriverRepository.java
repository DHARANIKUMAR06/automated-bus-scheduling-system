package com.dtc.busmanagement.repository;

import com.dtc.busmanagement.entity.Driver;
import com.dtc.busmanagement.enums.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByUserId(Long userId);
    List<Driver> findByStatus(DriverStatus status);
    List<Driver> findByDepotId(Long depotId);
    List<Driver> findByDepotIdAndStatus(Long depotId, DriverStatus status);
    long countByStatus(DriverStatus status);

    @Query("SELECT d FROM Driver d WHERE d.licenseExpiryDate <= :date")
    List<Driver> findDriversWithLicenseExpiringBefore(@Param("date") LocalDate date);

    @Query("SELECT d FROM Driver d WHERE LOWER(d.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(d.licenseNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Driver> searchDrivers(@Param("search") String search);
}
