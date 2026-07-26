package com.dtc.busmanagement.service;

import com.dtc.busmanagement.dto.TripDTO;
import java.time.LocalDate;
import java.util.List;

public interface TripService {
    List<TripDTO> getActiveTrips();
    List<TripDTO> getAllTripsForDate(LocalDate date);
    TripDTO getTripById(Long id);
    TripDTO startTrip(Long id);
    TripDTO endTrip(Long id);
    TripDTO updateTripStatusAndDelay(Long id, String status, int delayMinutes, String currentStop, String notes);
}
