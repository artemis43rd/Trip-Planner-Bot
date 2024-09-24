package com.telegrambot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.telegrambot.entity.User;
import com.telegrambot.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public User create(long telegramId) {
        User user = new User();
        user.setTelegramId(telegramId);
        user.setScore(0);
        return userRepository.save(user);
    }

    @Override
    public User getUser(long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }
}
