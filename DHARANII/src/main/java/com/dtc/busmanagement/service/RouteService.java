package com.dtc.busmanagement.service;

import com.dtc.busmanagement.dto.RouteDTO;
import java.util.List;

public interface RouteService {
    List<RouteDTO> getAllRoutes(String search);
    RouteDTO getRouteById(Long id);
    RouteDTO createRoute(RouteDTO routeDTO);
    RouteDTO updateRoute(Long id, RouteDTO routeDTO);
    void deleteRoute(Long id);
}
