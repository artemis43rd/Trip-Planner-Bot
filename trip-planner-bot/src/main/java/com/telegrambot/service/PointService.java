package com.telegrambot.service;

import com.telegrambot.entity.Point;
import com.telegrambot.repository.PointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PointService {
    private final PointRepository pointRepository;

    @Autowired
    public PointService(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }

    // Метод для создания новой точки
    public Point createPoint(Point point) {
        return pointRepository.save(point);
    }
}
