package com.telegrambot.repository;

import com.telegrambot.entity.Trip;
import com.telegrambot.entity.User;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TripRepository extends JpaRepository<Trip, Integer> {

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.user.userId = :userId AND t.progress = 'finished'")
    int countFinishedTripsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) > 0 FROM Trip t WHERE t.nameTrip = :tripName AND t.user = :user")
    boolean isTripExists(@Param("tripName") String tripName, @Param("user") User user);

    @Query("SELECT t FROM Trip t WHERE t.user = :user AND t.progress = :progress")
    List<Trip> findTripsByUserAndProgress(@Param("user") User user, @Param("progress") String progress);

    @Query("SELECT t FROM Trip t WHERE t.user = :user")
    List<Trip> findTripsByUser(@Param("user") User user);

    @Query("SELECT t FROM Trip t WHERE t.nameTrip = :tripName AND t.user = :user")
    Trip findByNameAndUser(@Param("tripName") String tripName, @Param("user") User user);

    @Query("SELECT t FROM Trip t WHERE t.progress = :progress")
    List<Trip> findAllTripByProgress(@Param("progress") String progress);
}
