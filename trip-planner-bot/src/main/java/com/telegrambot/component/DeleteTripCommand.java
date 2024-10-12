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
        boolean success = trips.deleteTrip(tripName, user.getId());

        StringBuilder builder = new StringBuilder();
        if (success) {
            builder.append("Trip \"").append(tripName).append("\" has been deleted from Trip-list");
        } else {
            builder.append("Trip \"").append(tripName).append("\" hasn't been deleted from Trip-list.\n")
                .append("There is no trip with that name");
        }

        try {
            telegramClient.execute(new SendMessage(chat.getId().toString(), builder.toString()));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
