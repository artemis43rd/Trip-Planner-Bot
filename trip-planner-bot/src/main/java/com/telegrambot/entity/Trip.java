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

    // Конструктор по умолчанию
    public Trip() {}

    // Getters and Setters
    public Integer getTripId() {
        return tripId;
    }

    public void setTripId(Integer tripId) {
        this.tripId = tripId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getNameTrip() {
        return nameTrip;
    }

    public void setNameTrip(String nameTrip) {
        this.nameTrip = nameTrip;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }
}
