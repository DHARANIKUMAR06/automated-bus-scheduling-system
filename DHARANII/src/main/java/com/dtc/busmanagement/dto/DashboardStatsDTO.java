package com.dtc.busmanagement.dto;

import java.util.Map;

public class DashboardStatsDTO {
    private long totalBuses;
    private long activeBuses;
    private long totalDrivers;
    private long totalConductors;
    private long todayTrips;
    private long completedTrips;
    private long delayedTrips;
    private long activeNotifications;

    private Map<String, Long> busFuelTypeDistribution;
    private Map<String, Long> busStatusDistribution;
    private Map<String, Long> tripStatusDistribution;

    public DashboardStatsDTO() {}

    public long getTotalBuses() {
        return totalBuses;
    }

    public void setTotalBuses(long totalBuses) {
        this.totalBuses = totalBuses;
    }

    public long getActiveBuses() {
        return activeBuses;
    }

    public void setActiveBuses(long activeBuses) {
        this.activeBuses = activeBuses;
    }

    public long getTotalDrivers() {
        return totalDrivers;
    }

    public void setTotalDrivers(long totalDrivers) {
        this.totalDrivers = totalDrivers;
    }

    public long getTotalConductors() {
        return totalConductors;
    }

    public void setTotalConductors(long totalConductors) {
        this.totalConductors = totalConductors;
    }

    public long getTodayTrips() {
        return todayTrips;
    }

    public void setTodayTrips(long todayTrips) {
        this.todayTrips = todayTrips;
    }

    public long getCompletedTrips() {
        return completedTrips;
    }

    public void setCompletedTrips(long completedTrips) {
        this.completedTrips = completedTrips;
    }

    public long getDelayedTrips() {
        return delayedTrips;
    }

    public void setDelayedTrips(long delayedTrips) {
        this.delayedTrips = delayedTrips;
    }

    public long getActiveNotifications() {
        return activeNotifications;
    }

    public void setActiveNotifications(long activeNotifications) {
        this.activeNotifications = activeNotifications;
    }

    public Map<String, Long> getBusFuelTypeDistribution() {
        return busFuelTypeDistribution;
    }

    public void setBusFuelTypeDistribution(Map<String, Long> busFuelTypeDistribution) {
        this.busFuelTypeDistribution = busFuelTypeDistribution;
    }

    public Map<String, Long> getBusStatusDistribution() {
        return busStatusDistribution;
    }

    public void setBusStatusDistribution(Map<String, Long> busStatusDistribution) {
        this.busStatusDistribution = busStatusDistribution;
    }

    public Map<String, Long> getTripStatusDistribution() {
        return tripStatusDistribution;
    }

    public void setTripStatusDistribution(Map<String, Long> tripStatusDistribution) {
        this.tripStatusDistribution = tripStatusDistribution;
    }
}
