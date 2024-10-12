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
public class AddTripCommand extends BotCommand {

    private final TripService trips;

    public AddTripCommand(TripService trips) {
        super("create_trip", "Add trip to the Trip-list\nUsage example: /create_trip \"Trip Name\"");
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
        boolean success = trips.createTrip(tripName, user.getId());

        StringBuilder builder = new StringBuilder();
        if (success) {
            builder.append("Trip \"").append(tripName).append("\" has been added to Trip-list");
        } else {
            builder.append("Trip \"").append(tripName).append("\" hasn't been added to Trip-list.\n")
                .append("This name for trip is already used");
        }

        try {
            telegramClient.execute(new SendMessage(chat.getId().toString(), builder.toString()));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
