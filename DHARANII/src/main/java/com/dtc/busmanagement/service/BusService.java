package com.dtc.busmanagement.service;

import com.dtc.busmanagement.dto.BusDTO;
import java.util.List;

public interface BusService {
    List<BusDTO> getAllBuses(String search);
    BusDTO getBusById(Long id);
    BusDTO createBus(BusDTO busDTO);
    BusDTO updateBus(Long id, BusDTO busDTO);
    void deleteBus(Long id);
    List<BusDTO> getBusesByStatus(String status);
    List<BusDTO> getBusesByDepot(Long depotId);
}
