package com.dtc.busmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "stops", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"route_id", "sequence_number"})
})
public class Stop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    @JsonIgnore
    private Route route;

    @Column(name = "stop_name", nullable = false, length = 100)
    private String stopName;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(name = "distance_from_start", nullable = false)
    private Double distanceFromStart;

    @Column(name = "estimated_time_from_start", nullable = false)
    private Integer estimatedTimeFromStart; // in minutes

    public Stop() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
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
