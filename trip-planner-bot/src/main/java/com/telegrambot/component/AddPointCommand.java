package com.telegrambot.component;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.telegrambot.service.PointService;
import com.telegrambot.service.TripService;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@Component
public class AddPointCommand extends BotCommand {

    private final PointService points;
    private final TripService trips;

    public AddPointCommand(PointService points, TripService trips) {
        super("create_point", "Add point to the trip" +
            "\nUsage example: /create_point \"Trip Name\" \"Point Name\"\n\"Date\" \"Transport\" \"Notes (optional)\"" +
            "\nTime format: yyyy-MM-dd HH:mm:ss");
            this.points = points;
            this.trips = trips;
    }

    @Override
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        if (strings.length == 0) {
            try {
                telegramClient.execute(new SendMessage(chat.getId().toString(), "Please provide the trip details in the following format:" +
                    "\n/create_point \"Trip Name\" \"Point Name\" \"Date\" \"Transport\" \"Notes (optional)\""));
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
        if (arguments.size() < 4) {
            try {
                telegramClient.execute(new SendMessage(chat.getId().toString(), "Please provide all the required details:\n" +
                    "\"Trip Name\" \"Point Name\" \"Date\" \"Transport\""));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;
        } else if (arguments.size() > 4) {
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
        String transport = arguments.get(3);
        String notes = arguments.size() > 4 ? arguments.get(4) : "";

        SendMessage message = new SendMessage(chat.getId().toString(), "Unknown Error!");
        int resTrip = trips.createTrip(tripName, user.getId());
        if (resTrip == 1) {
            int res = points.createPoint(tripName, pointName, date, transport, notes, user.getId());
            switch (res) {
                case 0:
                    String sendMmessage = String.format("Trip Name: %s\nPoint Name: %s\nDate: %s\nTransport: %s\nNotes: " +
                        "%s\n\nDo you want to proceed?", tripName, pointName, date, transport, notes);
                    // Создаем клавиатуру с кнопками
                    InlineKeyboardMarkup inlineKeyboardMarkup = createConfirmationKeyboard(tripName, pointName, date);
                    message = new SendMessage(chat.getId().toString(), sendMmessage);
                    message.setReplyMarkup(inlineKeyboardMarkup);
                    break;

                case 1:
                    message = new SendMessage(chat.getId().toString(), "Point with this name and date already exists");
                    break;

                case 2:
                    message = new SendMessage(chat.getId().toString(), "SQL Query Error");
                    break;

                case 4:
                    message = new SendMessage(chat.getId().toString(), "You are not registered yet! Try /start command");
                    break;

                case 5:
                    message = new SendMessage(chat.getId().toString(), "You entered the date later than the current moment");
                    break;

                case 6:
                    message = new SendMessage(chat.getId().toString(), "Incorrect time format. Try: yyyy-MM-dd HH:mm:ss");
                    break;

                default:
                    break;
            }

        }  else if (resTrip == 0) {
            trips.deleteTrip(tripName, user.getId());
            message = new SendMessage(chat.getId().toString(), "There is no trip with that name");

        }  else if (resTrip == 4) {
            message = new SendMessage(chat.getId().toString(), "You are not registered yet! Try /start command");
        }

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private InlineKeyboardMarkup createConfirmationKeyboard(String tripName, String pointName, String date) {

        InlineKeyboardButton confirmButton = new InlineKeyboardButton("✅");
        confirmButton.setCallbackData("addPoint:confirm");
        //Это по сути костыль, так как создание происходит сразу, а только при нажатии x она удалится
        //Это произошло из-за ограничения на размер setCallbackData
        InlineKeyboardButton cancelButton = new InlineKeyboardButton("❌");
        cancelButton.setCallbackData("addP:c" + tripName + ";" + pointName + ";" + date + ";");

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(confirmButton);
        row1.add(cancelButton);

        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(row1));

        return new InlineKeyboardMarkup(rows);
    }
}
