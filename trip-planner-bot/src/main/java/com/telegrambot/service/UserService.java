package com.telegrambot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.telegrambot.entity.User;
import com.telegrambot.repository.TripRepository;
import com.telegrambot.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;

    @Autowired
    public UserService(UserRepository userRepository, TripRepository tripRepository) {
        this.userRepository = userRepository;
        this.tripRepository = tripRepository;
    }

    @Transactional
    public User create(long telegramId) {
        User user = new User();
        user.setTelegramId(telegramId);
        user.setScore(0);
        return userRepository.save(user);
    }

    public User getUser(long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    @Transactional
    public int getUserScore(long telegramId) {
        User user = getUser(telegramId);
        if (user != null) {
            int finishedTripsCount = tripRepository.countFinishedTripsByUserId(user.getUserId());
            user.setScore(finishedTripsCount);
            userRepository.save(user); // Обновляем запись в базе
            return finishedTripsCount;
        }
        return 0;
    }
}
