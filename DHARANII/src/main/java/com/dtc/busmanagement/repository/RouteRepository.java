package com.dtc.busmanagement.repository;

import com.dtc.busmanagement.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    Optional<Route> findByRouteNumber(String routeNumber);

    @Query("SELECT r FROM Route r WHERE LOWER(r.routeNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.startLocation) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.endLocation) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Route> searchRoutes(@Param("search") String search);
}
