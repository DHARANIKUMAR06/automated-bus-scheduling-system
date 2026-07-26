package com.dtc.busmanagement.service.impl;

import com.dtc.busmanagement.dto.BusDTO;
import com.dtc.busmanagement.entity.AuditLog;
import com.dtc.busmanagement.entity.Bus;
import com.dtc.busmanagement.entity.Depot;
import com.dtc.busmanagement.enums.BusStatus;
import com.dtc.busmanagement.enums.FuelType;
import com.dtc.busmanagement.exception.BadRequestException;
import com.dtc.busmanagement.exception.ResourceNotFoundException;
import com.dtc.busmanagement.repository.AuditLogRepository;
import com.dtc.busmanagement.repository.BusRepository;
import com.dtc.busmanagement.repository.DepotRepository;
import com.dtc.busmanagement.service.BusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BusServiceImpl implements BusService {

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private DepotRepository depotRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    private String getCurrentUsername() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return "system";
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private BusDTO convertToDTO(Bus bus) {
        BusDTO dto = new BusDTO();
        dto.setId(bus.getId());
        dto.setBusNumber(bus.getBusNumber());
        dto.setModel(bus.getModel());
        dto.setCapacity(bus.getCapacity());
        dto.setFuelType(bus.getFuelType().name());
        dto.setStatus(bus.getStatus().name());
        dto.setMileageKm(bus.getMileageKm());
        dto.setLastServiceDate(bus.getLastServiceDate());
        if (bus.getDepot() != null) {
            dto.setDepotId(bus.getDepot().getId());
            dto.setDepotName(bus.getDepot().getName());
        }
        return dto;
    }

    private Bus convertToEntity(BusDTO dto) {
        Bus bus = new Bus();
        bus.setBusNumber(dto.getBusNumber());
        bus.setModel(dto.getModel());
        bus.setCapacity(dto.getCapacity());
        bus.setFuelType(FuelType.valueOf(dto.getFuelType().toUpperCase()));
        bus.setStatus(BusStatus.valueOf(dto.getStatus().toUpperCase()));
        bus.setMileageKm(dto.getMileageKm());
        bus.setLastServiceDate(dto.getLastServiceDate());
        if (dto.getDepotId() != null) {
            Depot depot = depotRepository.findById(dto.getDepotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Depot not found with id: " + dto.getDepotId()));
            bus.setDepot(depot);
        }
        return bus;
    }

    @Override
    public List<BusDTO> getAllBuses(String search) {
        List<Bus> buses;
        if (search != null && !search.trim().isEmpty()) {
            buses = busRepository.searchBuses(search);
        } else {
            buses = busRepository.findAll();
        }
        return buses.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public BusDTO getBusById(Long id) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found with id: " + id));
        return convertToDTO(bus);
    }

    @Override
    @Transactional
    public BusDTO createBus(BusDTO busDTO) {
        if (busRepository.findByBusNumber(busDTO.getBusNumber()).isPresent()) {
            throw new BadRequestException("Bus with registration plate " + busDTO.getBusNumber() + " already exists.");
        }
        Bus bus = convertToEntity(busDTO);
        Bus savedBus = busRepository.save(bus);

        // Audit Log
        AuditLog log = new AuditLog(getCurrentUsername(), "BUS_CREATE", "Added bus " + savedBus.getBusNumber(), "127.0.0.1");
        auditLogRepository.save(log);

        return convertToDTO(savedBus);
    }

    @Override
    @Transactional
    public BusDTO updateBus(Long id, BusDTO busDTO) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found with id: " + id));

        bus.setBusNumber(busDTO.getBusNumber());
        bus.setModel(busDTO.getModel());
        bus.setCapacity(busDTO.getCapacity());
        bus.setFuelType(FuelType.valueOf(busDTO.getFuelType().toUpperCase()));
        bus.setStatus(BusStatus.valueOf(busDTO.getStatus().toUpperCase()));
        bus.setMileageKm(busDTO.getMileageKm());
        bus.setLastServiceDate(busDTO.getLastServiceDate());

        if (busDTO.getDepotId() != null) {
            Depot depot = depotRepository.findById(busDTO.getDepotId())
                    .orElseThrow(() -> new ResourceNotFoundException("Depot not found with id: " + busDTO.getDepotId()));
            bus.setDepot(depot);
        } else {
            bus.setDepot(null);
        }

        Bus updatedBus = busRepository.save(bus);

        // Audit Log
        AuditLog log = new AuditLog(getCurrentUsername(), "BUS_UPDATE", "Updated bus " + updatedBus.getBusNumber(), "127.0.0.1");
        auditLogRepository.save(log);

        return convertToDTO(updatedBus);
    }

    @Override
    @Transactional
    public void deleteBus(Long id) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found with id: " + id));
        busRepository.delete(bus);

        // Audit Log
        AuditLog log = new AuditLog(getCurrentUsername(), "BUS_DELETE", "Deleted bus " + bus.getBusNumber(), "127.0.0.1");
        auditLogRepository.save(log);
    }

    @Override
    public List<BusDTO> getBusesByStatus(String status) {
        BusStatus busStatus = BusStatus.valueOf(status.toUpperCase());
        return busRepository.findByStatus(busStatus).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<BusDTO> getBusesByDepot(Long depotId) {
        return busRepository.findByDepotId(depotId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }
}
