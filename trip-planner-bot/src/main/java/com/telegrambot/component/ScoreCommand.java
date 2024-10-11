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
public class ScoreCommand extends BotCommand {

    private final UserService users;

    public ScoreCommand(UserService users) {
        super("show_score", "Show user's finished trip score.");
        this.users = users;
    }

    @Override
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        StringBuilder builder = new StringBuilder();

        builder.append(user.getFirstName()).append(" your finished trips score is: ").append(users.updateUserScore(user.getId()));

        try {
            telegramClient.execute(new SendMessage(chat.getId().toString(), builder.toString()));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
