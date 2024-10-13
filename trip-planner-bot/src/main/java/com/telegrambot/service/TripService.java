package com.telegrambot.service;

import com.telegrambot.entity.Point;
import com.telegrambot.entity.Trip;
import com.telegrambot.entity.User;
import com.telegrambot.repository.PointRepository;
import com.telegrambot.repository.TripRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TripService {

    private final UserService userService;
    private final TripRepository tripRepository;
    private final PointRepository pointRepository; 

    @Autowired
    public TripService(UserService userService,
		TripRepository tripRepository, PointRepository pointRepository) {
        this.userService = userService;
        this.tripRepository = tripRepository;
        this.pointRepository = pointRepository;
    }

    @Transactional
    public int createTrip(String tripName, long telegramId) {
        User user = userService.getUser(telegramId);
		if (user == null) {
			return 4;
		}

		if (tripRepository.isTripExists(tripName, user)) {
			return 1;
		}

		Trip trip = new Trip();
		trip.setUser(user);
		trip.setNameTrip(tripName);
		trip.setProgress("planned");

		tripRepository.save(trip);
		if (tripRepository.isTripExists(tripName, user)) {
			return 0;
		}
		return 2;
	}

	public List<Trip> getTripsByProgress(String progress, Long chatId) {
		if (progress == "all") {
			User user = userService.getUser(chatId);
        	return tripRepository.findTripsByUser(user);
		} else {
			User user = userService.getUser(chatId);
			return tripRepository.findTripsByUserAndProgress(user, progress);
		}
	}

    @Transactional
	public int deleteTrip(String tripName, long telegramId) {
		User user = userService.getUser(telegramId);
		if (user == null) {
			return 4;
		}

		Trip trip = tripRepository.findByNameAndUser(tripName, user);
		if (trip == null) {
			return 1;
		}

		tripRepository.delete(trip);
		trip = tripRepository.findByNameAndUser(tripName, user);
		if (trip == null) {
			return 0;
		}
		return 2;
	}

	public List<Point> getPointsByTrip(String tripName, long telegramId) {
		User user = userService.getUser(telegramId);
		Trip trip = tripRepository.findByNameAndUser(tripName, user);
        return pointRepository.findAllByTripOrderByDate(trip);
    }

	public List<Trip> getTripsWithProgress(String progress) {
        return tripRepository.findAllTripByProgress(progress);
    }

	public void saveTrip(Trip trip) {
        tripRepository.save(trip);
    }
}