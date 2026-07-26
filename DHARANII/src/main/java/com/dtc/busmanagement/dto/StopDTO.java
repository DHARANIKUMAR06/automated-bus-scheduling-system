package com.dtc.busmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StopDTO {
    private Long id;

    @NotBlank(message = "Stop name is required")
    private String stopName;

    @NotNull(message = "Sequence number is required")
    private Integer sequenceNumber;

    @NotNull(message = "Distance from start is required")
    private Double distanceFromStart;

    @NotNull(message = "Estimated time from start is required")
    private Integer estimatedTimeFromStart; // in minutes

    public StopDTO() {}

    public StopDTO(Long id, String stopName, Integer sequenceNumber, Double distanceFromStart, Integer estimatedTimeFromStart) {
        this.id = id;
        this.stopName = stopName;
        this.sequenceNumber = sequenceNumber;
        this.distanceFromStart = distanceFromStart;
        this.estimatedTimeFromStart = estimatedTimeFromStart;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Double getDistanceFromStart() {
        return distanceFromStart;
    }

    public void setDistanceFromStart(Double distanceFromStart) {
        this.distanceFromStart = distanceFromStart;
    }

    public Integer getEstimatedTimeFromStart() {
        return estimatedTimeFromStart;
    }

    public void setEstimatedTimeFromStart(Integer estimatedTimeFromStart) {
        this.estimatedTimeFromStart = estimatedTimeFromStart;
    }
}
