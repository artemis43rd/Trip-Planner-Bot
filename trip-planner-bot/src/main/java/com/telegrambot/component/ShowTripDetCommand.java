package com.telegrambot.component;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.telegrambot.entity.Point;
import com.telegrambot.service.TripService;

import java.util.List;

@Component
public class ShowTripDetCommand extends BotCommand {
    
    private final TripService trips;
    
    public ShowTripDetCommand(TripService trips) {
        super("show_trip_detail", "Show all points in trip with additional information" +
        "\nUsage example: /show_trip_detail \"Trip Name\"");
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
        StringBuilder builder = new StringBuilder();

        int res = trips.createTrip(tripName, user.getId());
        if (res == 1) {
            List<Point> points = trips.getPointsByTrip(tripName, user.getId());
            int counter = 1;
            for (Point point : points) {
                builder.append("[").append(counter).append("]\nPoint: ").append(point.getNamePoint())
                    .append("\nDate: ").append(point.getPointDate())
                    .append("\nTransport: ").append(point.getTransport());

                    if (point.getNotes().length() == 0)
                    {
                        builder.append("\nNotes: ").append(point.getNotes());
                    }

                    builder.append("\nVisited: ");
                    if (point.getVisited()) {
                        builder.append("✅\n\n");
                    } else {
                        builder.append("❌\n\n");
                    }
                counter++;
            }
        } else if (res == 0) {
            trips.deleteTrip(tripName, user.getId());
            builder.append("There is no trip with that name");
        }

        try {
            telegramClient.execute(new SendMessage(chat.getId().toString(), builder.toString()));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
