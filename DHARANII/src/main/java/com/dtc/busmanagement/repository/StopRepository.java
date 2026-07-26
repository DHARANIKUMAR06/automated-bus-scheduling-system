package com.dtc.busmanagement.repository;

import com.dtc.busmanagement.entity.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StopRepository extends JpaRepository<Stop, Long> {
    List<Stop> findByRouteIdOrderBySequenceNumberAsc(Long routeId);
    void deleteByRouteId(Long routeId);
}
