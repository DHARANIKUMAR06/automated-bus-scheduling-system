package com.dtc.busmanagement.repository;

import com.dtc.busmanagement.entity.Bus;
import com.dtc.busmanagement.enums.BusStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {
    Optional<Bus> findByBusNumber(String busNumber);
    List<Bus> findByStatus(BusStatus status);
    List<Bus> findByDepotId(Long depotId);
    List<Bus> findByDepotIdAndStatus(Long depotId, BusStatus status);
    long countByStatus(BusStatus status);

    @Query("SELECT b FROM Bus b WHERE LOWER(b.busNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(b.model) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Bus> searchBuses(@Param("search") String search);
}
