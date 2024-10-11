package com.telegrambot.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "points")
public class Point {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pointId;

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(name = "name_point", nullable = false)
    private String namePoint;

    @Column(name = "point_date", nullable = false)
    private Timestamp pointDate;

    @Column(name = "transport", nullable = false)
    private String transport;

    @Column(name = "notes")
    private String notes;

    // Конструктор по умолчанию
    public Point() {}

    // Getters and Setters
    public Integer getPointId() {
        return pointId;
    }

    public void setPointId(Integer pointId) {
        this.pointId = pointId;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public String getNamePoint() {
        return namePoint;
    }

    public void setNamePoint(String namePoint) {
        this.namePoint = namePoint;
    }

    public Timestamp getPointDate() {
        return pointDate;
    }

    public void setPointDate(Timestamp pointDate) {
        this.pointDate = pointDate;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
