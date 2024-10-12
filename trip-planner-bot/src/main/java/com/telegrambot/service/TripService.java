package com.telegrambot.service;

import com.telegrambot.entity.Trip;
import com.telegrambot.entity.User;
import com.telegrambot.repository.TripRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TripService {

    private final UserService userService;
    private final TripRepository tripRepository;

    @Autowired
    public TripService(UserService userService, TripRepository tripRepository) {
        this.userService = userService;
        this.tripRepository = tripRepository;
    }

    @Transactional
    public boolean createTrip(String tripName, long telegramId) {
        User user = userService.getUser(telegramId);
		if (user == null) {
			return false;
		}

		if (tripRepository.isTripExists(tripName, user)) {
			return false;
		}

		Trip trip = new Trip();
		trip.setUser(user);
		trip.setNameTrip(tripName);
		trip.setProgress("planned");

		tripRepository.save(trip);
		return true;
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
	public boolean deleteTrip(String tripName, long telegramId) {
		User user = userService.getUser(telegramId);
		if (user == null) {
			return false;
		}

		Trip trip = tripRepository.findByNameAndUser(tripName, user);
		if (trip == null) {
			return false;
		}

		tripRepository.delete(trip);
		return true;
	}
}