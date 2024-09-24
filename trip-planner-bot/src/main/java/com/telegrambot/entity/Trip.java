package com.telegrambot.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "trips")
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tripId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name_trip", nullable = false)
    private String nameTrip;

    @Column(nullable = false)
    private String progress;

    // Getters and Setters
}
