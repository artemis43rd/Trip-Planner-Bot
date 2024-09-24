package com.telegrambot.component;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import com.telegrambot.service.UserService;

@Component
public class StartCommand extends BotCommand {

    private final UserService users;

    public StartCommand(UserService users) {
        super("start", "Register in the bot.");
        this.users = users;
    }

    @Override
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        boolean isNewUser = signUp(user); // Возвращает true, если пользователь новый, иначе false.

        StringBuilder builder = new StringBuilder();
        if (isNewUser) {
            builder.append("Welcome ").append(user.getFirstName()).append(" to Trip Planner Bot!\nYou have been registered as: ").append(user.getUserName());
        } else {
            builder.append("Welcome back, ").append(user.getFirstName()).append("! You are already registered.");
        }

        try {
            telegramClient.execute(new SendMessage(chat.getId().toString(), builder.toString()));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean signUp(User user) {
        // Проверяем, существует ли пользователь
        if (users.getUser(user.getId()) == null) {
            // Если пользователя нет, создаем его
            users.create(user.getId());
            System.out.println("New user created with ID: " + user.getId());
            return true; // Новый пользователь
        }
        return false; // Пользователь уже существует
    }
}
