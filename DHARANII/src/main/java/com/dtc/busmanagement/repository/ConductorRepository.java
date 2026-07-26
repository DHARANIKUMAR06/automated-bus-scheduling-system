package com.dtc.busmanagement.repository;

import com.dtc.busmanagement.entity.Conductor;
import com.dtc.busmanagement.enums.ConductorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConductorRepository extends JpaRepository<Conductor, Long> {
    Optional<Conductor> findByUserId(Long userId);
    Optional<Conductor> findByEmployeeId(String employeeId);
    List<Conductor> findByStatus(ConductorStatus status);
    List<Conductor> findByDepotId(Long depotId);
    List<Conductor> findByDepotIdAndStatus(Long depotId, ConductorStatus status);
    long countByStatus(ConductorStatus status);

    @Query("SELECT c FROM Conductor c WHERE LOWER(c.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.employeeId) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Conductor> searchConductors(@Param("search") String search);
}
