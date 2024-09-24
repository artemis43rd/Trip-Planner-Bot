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

    // Getters and Setters
}
