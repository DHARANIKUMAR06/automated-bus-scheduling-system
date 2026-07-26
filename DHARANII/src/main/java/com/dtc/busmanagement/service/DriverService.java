package com.dtc.busmanagement.service;

import com.dtc.busmanagement.dto.DriverDTO;
import java.util.List;

public interface DriverService {
    List<DriverDTO> getAllDrivers(String search);
    DriverDTO getDriverById(Long id);
    DriverDTO createDriver(DriverDTO driverDTO);
    DriverDTO updateDriver(Long id, DriverDTO driverDTO);
    void deleteDriver(Long id);
    List<DriverDTO> getDriversByStatus(String status);
    List<DriverDTO> getDriversByDepot(Long depotId);
    List<DriverDTO> getLicenseExpiringDrivers();
}
