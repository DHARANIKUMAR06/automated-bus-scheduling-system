package com.dtc.busmanagement.service;

import com.dtc.busmanagement.dto.ConductorDTO;
import java.util.List;

public interface ConductorService {
    List<ConductorDTO> getAllConductors(String search);
    ConductorDTO getConductorById(Long id);
    ConductorDTO createConductor(ConductorDTO conductorDTO);
    ConductorDTO updateConductor(Long id, ConductorDTO conductorDTO);
    void deleteConductor(Long id);
    List<ConductorDTO> getConductorsByStatus(String status);
    List<ConductorDTO> getConductorsByDepot(Long depotId);
}
