package com.dtc.busmanagement.service;

import com.dtc.busmanagement.dto.ScheduleDTO;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {
    List<ScheduleDTO> getAllSchedules(LocalDate date);
    ScheduleDTO getScheduleById(Long id);
    ScheduleDTO createSchedule(ScheduleDTO scheduleDTO);
    ScheduleDTO updateSchedule(Long id, ScheduleDTO scheduleDTO);
    void deleteSchedule(Long id);
    List<ScheduleDTO> autoGenerateSchedules(LocalDate date, Long routeId);
}
