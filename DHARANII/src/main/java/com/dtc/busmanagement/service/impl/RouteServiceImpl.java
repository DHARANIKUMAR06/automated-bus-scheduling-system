package com.dtc.busmanagement.service.impl;

import com.dtc.busmanagement.dto.RouteDTO;
import com.dtc.busmanagement.dto.StopDTO;
import com.dtc.busmanagement.entity.AuditLog;
import com.dtc.busmanagement.entity.Route;
import com.dtc.busmanagement.entity.Stop;
import com.dtc.busmanagement.enums.RouteStatus;
import com.dtc.busmanagement.exception.BadRequestException;
import com.dtc.busmanagement.exception.ResourceNotFoundException;
import com.dtc.busmanagement.repository.AuditLogRepository;
import com.dtc.busmanagement.repository.RouteRepository;
import com.dtc.busmanagement.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RouteServiceImpl implements RouteService {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private String getCurrentUsername() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return "system";
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private RouteDTO convertToDTO(Route route) {
        RouteDTO dto = new RouteDTO();
        dto.setId(route.getId());
        dto.setRouteNumber(route.getRouteNumber());
        dto.setStartLocation(route.getStartLocation());
        dto.setEndLocation(route.getEndLocation());
        dto.setDistanceKm(route.getDistanceKm());
        dto.setEstimatedDurationMins(route.getEstimatedDurationMins());
        dto.setStatus(route.getStatus().name());

        List<StopDTO> stopDTOs = route.getStops().stream()
                .map(s -> new StopDTO(s.getId(), s.getStopName(), s.getSequenceNumber(), s.getDistanceFromStart(), s.getEstimatedTimeFromStart()))
                .collect(Collectors.toList());
        dto.setStops(stopDTOs);
        return dto;
    }

    @Override
    public List<RouteDTO> getAllRoutes(String search) {
        List<Route> routes;
        if (search != null && !search.trim().isEmpty()) {
            routes = routeRepository.searchRoutes(search);
        } else {
            routes = routeRepository.findAll();
        }
        return routes.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public RouteDTO getRouteById(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));
        return convertToDTO(route);
    }

    @Override
    @Transactional
    public RouteDTO createRoute(RouteDTO dto) {
        if (routeRepository.findByRouteNumber(dto.getRouteNumber()).isPresent()) {
            throw new BadRequestException("Route number " + dto.getRouteNumber() + " already exists.");
        }

        Route route = new Route();
        route.setRouteNumber(dto.getRouteNumber());
        route.setStartLocation(dto.getStartLocation());
        route.setEndLocation(dto.getEndLocation());
        route.setDistanceKm(dto.getDistanceKm());
        route.setEstimatedDurationMins(dto.getEstimatedDurationMins());
        route.setStatus(RouteStatus.valueOf(dto.getStatus().toUpperCase()));

        if (dto.getStops() != null) {
            for (StopDTO sDto : dto.getStops()) {
                Stop stop = new Stop();
                stop.setStopName(sDto.getStopName());
                stop.setSequenceNumber(sDto.getSequenceNumber());
                stop.setDistanceFromStart(sDto.getDistanceFromStart());
                stop.setEstimatedTimeFromStart(sDto.getEstimatedTimeFromStart());
                stop.setRoute(route);
                route.getStops().add(stop);
            }
        }

        Route savedRoute = routeRepository.save(route);

        // Audit Log
        AuditLog log = new AuditLog(getCurrentUsername(), "ROUTE_CREATE", "Added route " + savedRoute.getRouteNumber(), "127.0.0.1");
        auditLogRepository.save(log);

        return convertToDTO(savedRoute);
    }

    @Override
    @Transactional
    public RouteDTO updateRoute(Long id, RouteDTO dto) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));

        route.setRouteNumber(dto.getRouteNumber());
        route.setStartLocation(dto.getStartLocation());
        route.setEndLocation(dto.getEndLocation());
        route.setDistanceKm(dto.getDistanceKm());
        route.setEstimatedDurationMins(dto.getEstimatedDurationMins());
        route.setStatus(RouteStatus.valueOf(dto.getStatus().toUpperCase()));

        // Clear existing stops and add new ones (cascade & orphan removal handles cleanup)
        route.getStops().clear();
        if (dto.getStops() != null) {
            for (StopDTO sDto : dto.getStops()) {
                Stop stop = new Stop();
                stop.setStopName(sDto.getStopName());
                stop.setSequenceNumber(sDto.getSequenceNumber());
                stop.setDistanceFromStart(sDto.getDistanceFromStart());
                stop.setEstimatedTimeFromStart(sDto.getEstimatedTimeFromStart());
                stop.setRoute(route);
                route.getStops().add(stop);
            }
        }

        Route updatedRoute = routeRepository.save(route);

        // Audit Log
        AuditLog log = new AuditLog(getCurrentUsername(), "ROUTE_UPDATE", "Updated route " + updatedRoute.getRouteNumber(), "127.0.0.1");
        auditLogRepository.save(log);

        return convertToDTO(updatedRoute);
    }

    @Override
    @Transactional
    public void deleteRoute(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id: " + id));
        routeRepository.delete(route);

        // Audit Log
        AuditLog log = new AuditLog(getCurrentUsername(), "ROUTE_DELETE", "Deleted route " + route.getRouteNumber(), "127.0.0.1");
        auditLogRepository.save(log);
    }
}
