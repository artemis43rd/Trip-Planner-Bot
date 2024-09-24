package com.telegrambot.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "app_users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "telegram_id", nullable = false)
    private Long telegramId;

    @Column(nullable = false)
    private Integer score;

    // Конструктор по умолчанию
    public User() {}

    // Getters
    public Long getUserId() {
        return userId;
    }

    public Long getTelegramId() {
        return telegramId;
    }

    public Integer getScore() {
        return score;
    }

    // Setters
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setTelegramId(Long telegramId) {
        this.telegramId = telegramId;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}