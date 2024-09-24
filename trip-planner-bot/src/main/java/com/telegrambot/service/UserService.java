package com.telegrambot.service;

import com.telegrambot.entity.User;

public interface UserService {
    User create(long telegramId);
    User getUser(long telegramId);
}
