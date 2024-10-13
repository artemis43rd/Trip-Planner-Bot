package com.telegrambot.service;

import com.telegrambot.entity.Point;
import com.telegrambot.entity.Trip;
import com.telegrambot.entity.User;
import com.telegrambot.repository.PointRepository;
import com.telegrambot.repository.TripRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Service
public class PointService {

    private final TripRepository tripRepository;
    private final PointRepository pointRepository;
    private final UserService userService;

    @Autowired
    public PointService(UserService userService, TripRepository tripRepository,
        PointRepository pointRepository) {
        this.tripRepository = tripRepository;
        this.userService = userService;
        this.pointRepository = pointRepository;
    }

    @Transactional
    public int createPoint(String tripName, String pointName, String date,
                            String transport, String notes, long telegramId) {

        User user = userService.getUser(telegramId);
		if (user == null) {
			return 4;
		}

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime;
        try {
            localDateTime = LocalDateTime.parse(date, formatter);
        } catch (DateTimeParseException e) {
            return 6;
        }
        Timestamp inputTimestamp = Timestamp.valueOf(localDateTime);
        // Получаем текущий момент времени
        Timestamp currentTimestamp = Timestamp.valueOf(LocalDateTime.now());
        // Проверяем, что дата больше текущего момента
        if (!inputTimestamp.after(currentTimestamp)) {
            return 5;
        }

        Trip trip = tripRepository.findByNameAndUser(tripName, user);
        if (pointRepository.isPointExists(trip, pointName, inputTimestamp)) {
			return 1;
		}

        Point point = new Point();
        point.setTrip(trip);
        point.setNamePoint(pointName);
        point.setPointDate(inputTimestamp);
        point.setTransport(transport);
        point.setNotes(notes);

        pointRepository.save(point);
        if (pointRepository.isPointExists(trip, pointName, inputTimestamp)) {
			return 0;
		}

        return 2;
    }

    @Transactional
    public int deletePoint(String tripName, String pointName,
                            String date, long telegramId) {

        User user = userService.getUser(telegramId);
        if (user == null) {
            return 4;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime;
        try {
            localDateTime = LocalDateTime.parse(date, formatter);
        } catch (DateTimeParseException e) {
            return 6;
        }
        Timestamp inputTimestamp = Timestamp.valueOf(localDateTime);

        Trip trip = tripRepository.findByNameAndUser(tripName, user);
        if (!pointRepository.isPointExists(trip, pointName, inputTimestamp)) {
			return 1;
		}

        Point point = pointRepository.findByNameAndTime(trip, pointName, inputTimestamp);
        pointRepository.delete(point);

        if (!pointRepository.isPointExists(trip, pointName, inputTimestamp)) {
			return 0;
		}

        return 2;
    }

    @Transactional
    public int changeOneParam(String tripName, String pointName, String date,
                            String type, String value,long telegramId) {

        User user = userService.getUser(telegramId);
        if (user == null) {
            return 4;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime;
        try {
            localDateTime = LocalDateTime.parse(date, formatter);
        } catch (DateTimeParseException e) {
            return 6;
        }
        Timestamp inputTimestamp = Timestamp.valueOf(localDateTime);

        Trip trip = tripRepository.findByNameAndUser(tripName, user);
        if (!pointRepository.isPointExists(trip, pointName, inputTimestamp)) {
			return 1;
		}

        Point point = pointRepository.findByNameAndTime(trip, pointName, inputTimestamp);
        switch (type) {
            case "notes":
                point.setNotes(value);
                pointRepository.save(point);
                return 0;
            case "visited":
                if (value == "1") {
                    point.setVisited(true);
                    pointRepository.save(point);
                    return 0;
                } else {
                    return 7;
                }
            //if need add more changing parameters
            default:
                break;
        }

        return 2;
    }

    public void savePoint (Point point) {
        pointRepository.save(point);
    }
}
