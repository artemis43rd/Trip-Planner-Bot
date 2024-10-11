package com.telegrambot.repository;

import com.telegrambot.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TripRepository extends JpaRepository<Trip, Integer> {

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.user.userId = :userId AND t.progress = 'finished'")
    int countFinishedTripsByUserId(@Param("userId") Long userId);
}
