package com.telegrambot.component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.telegrambot.service.PointService;
import com.telegrambot.service.TripService;

@Component
public class ChangeVisCommand extends BotCommand {

    private final TripService trips;
    private final PointService points;

    public ChangeVisCommand(PointService points, TripService trips) {
        super("set_visited_state", "Set visited state for the point" +
        "\nUsage example: /set_visited_state \"Trip Name\" \"Point Name\" \"Date\"");
        this.points = points;
        this.trips = trips;
    }

    @Override
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        if (strings.length == 0) {
            try {
                telegramClient.execute(new SendMessage(chat.getId().toString(), "Please provide the trip details in the following format:" +
                    "\n/set_visited_state \"Trip Name\" \"Point Name\" \"Date\""));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        String input = String.join(" ", strings);
        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(input);

        List<String> arguments = new ArrayList<>();
        while (matcher.find()) {
            arguments.add(matcher.group(1));
        }

        // Проверяем, что все необходимые аргументы были предоставлены
        if (arguments.size() < 3) {
            try {
                telegramClient.execute(new SendMessage(chat.getId().toString(), "Please provide all the required details:\n" +
                    "\"Trip Name\" \"Point Name\" \"Date\""));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;
        } else if (arguments.size() > 3) {
            try {
                telegramClient.execute(new SendMessage(chat.getId().toString(), "Warning! The number of arguments is more than" +
                    "the command can accept, the extra ones will not be used"));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

        String tripName = arguments.get(0);
        String pointName = arguments.get(1);
        String date = arguments.get(2);

        StringBuilder builder = new StringBuilder();
        int resTrip = trips.createTrip(tripName, user.getId());
        if (resTrip == 1) {
            int res = points.changeOneParam(tripName, pointName, date, "visited", "1", user.getId());
            switch (res) {
                case 0:
                    builder.append("The note has been successfully changed");
                    break;

                case 1:
                    builder.append("There is no point with that name and time");
                    break;

                case 2:
                    builder.append("SQL Query Error");
                    break;

                case 4:
                    builder.append("You are not registered yet! Try /start command");
                    break;

                case 6:
                    builder.append("Incorrect time format. Try: yyyy-MM-dd HH:mm:ss");
                    break;

                case 7:
                    builder.append("Wrong value for visited. Change to 1");
                    break;

                default:
                    break;
            }

        }  else if (resTrip == 0) {
            trips.deleteTrip(tripName, user.getId());
            builder.append("There is no trip with that name");

        }  else if (resTrip == 4) {
            builder.append("You are not registered yet! Try /start command");
        }

        try {
            telegramClient.execute(new SendMessage(chat.getId().toString(), builder.toString()));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
