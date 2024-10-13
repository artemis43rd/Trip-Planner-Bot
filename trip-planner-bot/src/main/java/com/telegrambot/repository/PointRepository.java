package com.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.telegrambot.entity.Point;
import com.telegrambot.entity.Trip;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface PointRepository extends JpaRepository<Point, Integer> {
    @Query("SELECT p FROM Point p WHERE p.trip = :trip ORDER BY p.pointDate ASC")
    List<Point> findAllByTripOrderByDate(@Param("trip") Trip trip);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Point p WHERE p.trip = :trip AND p.namePoint = :namePoint AND p.pointDate = :pointDate")
    boolean isPointExists(@Param("trip") Trip trip, @Param("namePoint") String namePoint, @Param("pointDate") Timestamp pointDate);

    @Query("SELECT p FROM Point p WHERE p.trip = :trip AND p.namePoint = :namePoint AND p.pointDate = :pointDate")
    Point findByNameAndTime(@Param("trip") Trip trip, @Param("namePoint") String namePoint, @Param("pointDate") Timestamp pointDate);
}
