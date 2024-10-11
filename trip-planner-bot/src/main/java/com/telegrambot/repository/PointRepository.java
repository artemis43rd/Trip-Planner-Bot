package com.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.telegrambot.entity.Point;

@Repository
public interface PointRepository extends JpaRepository<Point, Integer> {
}
