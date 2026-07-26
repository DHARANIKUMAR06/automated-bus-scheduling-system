package com.dtc.busmanagement.repository;

import com.dtc.busmanagement.entity.MaintenanceRecord;
import com.dtc.busmanagement.enums.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {
    List<MaintenanceRecord> findByBusId(Long busId);
    List<MaintenanceRecord> findByStatus(MaintenanceStatus status);

    @Query("SELECT m FROM MaintenanceRecord m WHERE m.serviceDate BETWEEN :startDate AND :endDate")
    List<MaintenanceRecord> findByServiceDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT m FROM MaintenanceRecord m WHERE m.status = 'SCHEDULED' AND m.serviceDate <= :date")
    List<MaintenanceRecord> findUpcomingMaintenance(@Param("date") LocalDate date);
}
