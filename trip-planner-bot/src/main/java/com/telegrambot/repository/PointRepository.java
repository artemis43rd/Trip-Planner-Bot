package com.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.telegrambot.entity.Point;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface PointRepository extends JpaRepository<Point, Integer> {
    
    // Метод для поиска точек по tripId и диапазону дат
    //List<Point> findByTripIdAndPointDateBetween(Integer tripId, Timestamp startDate, Timestamp endDate);
}
