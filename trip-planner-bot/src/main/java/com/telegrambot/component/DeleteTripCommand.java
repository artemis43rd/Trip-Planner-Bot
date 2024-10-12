package com.telegrambot.component;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import com.telegrambot.service.TripService;

@Component
public class DeleteTripCommand extends BotCommand {

    private final TripService trips;

    public DeleteTripCommand(TripService trips) {
        super("delete_trip", "Delete trip from the Trip-list\nUsage example: /delete_trip \"Trip Name\"");
        this.trips = trips;
    }

    @Override
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        if (strings.length < 1) {
            try {
                telegramClient.execute(new SendMessage(chat.getId().toString(), "Please provide the name of the trip."));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        String tripName = strings[0].replaceAll("\"", ""); // Удаление кавычек, если они есть
        int res = trips.deleteTrip(tripName, user.getId());

        StringBuilder builder = new StringBuilder();
        switch (res) {
            case 0:
                builder.append("Trip \"").append(tripName).append("\" has been deleted to Trip-list");
                break;
            case 1:
                builder.append("Trip \"").append(tripName).append("\" hasn't been deleted to Trip-list.\n")
                    .append("There is no trip with that name");
                break;
            case 2:
                builder.append("Trip \"").append(tripName).append("\" hasn't been deleted to Trip-list.\n")
                    .append("SQL Query Error");
                break;
            case 4:
                builder.append("You are not registered yet! Try /start command");
                break;

            default:
                break;
        }

        try {
            telegramClient.execute(new SendMessage(chat.getId().toString(), builder.toString()));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
