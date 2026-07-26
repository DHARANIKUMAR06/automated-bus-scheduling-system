package com.dtc.busmanagement.entity;

import com.dtc.busmanagement.enums.ConductorStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "conductors")
public class Conductor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "employee_id", unique = true, nullable = false, length = 50)
    private String employeeId;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ConductorStatus status = ConductorStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "depot_id")
    private Depot depot;

    public Conductor() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public ConductorStatus getStatus() {
        return status;
    }

    public void setStatus(ConductorStatus status) {
        this.status = status;
    }

    public Depot getDepot() {
        return depot;
    }

    public void setDepot(Depot depot) {
        this.depot = depot;
    }
}
