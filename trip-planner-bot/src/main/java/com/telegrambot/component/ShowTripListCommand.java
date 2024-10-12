package com.telegrambot.component;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class ShowTripListCommand extends BotCommand {

    public ShowTripListCommand() {
        super("show_trips", "Show planned/in-progress/finished trips" +
        "\nform Trip-list or all of them");
    }

    @Override
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        String messageText = "Which trips do you want me to output?";

        // Создаем разметку с инлайн-кнопками
        InlineKeyboardMarkup markup = createInlineKeyboardMarkup();

        // Отправляем сообщение с разметкой
        SendMessage sendMessage = new SendMessage(chat.getId().toString(), messageText);
        sendMessage.setReplyMarkup(markup);

        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private InlineKeyboardMarkup createInlineKeyboardMarkup() {
        InlineKeyboardButton plannedButton = new InlineKeyboardButton("Planned");
        plannedButton.setCallbackData("showTrips:1");

        InlineKeyboardButton inProgressButton = new InlineKeyboardButton("In-Progress");
        inProgressButton.setCallbackData("showTrips:2");

        InlineKeyboardButton finishedButton = new InlineKeyboardButton("Finished");
        finishedButton.setCallbackData("showTrips:3");

        InlineKeyboardButton allButton = new InlineKeyboardButton("All");
        allButton.setCallbackData("showTrips:4");

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(plannedButton);
        row1.add(inProgressButton);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(finishedButton);
        row2.add(allButton);

        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.add(new InlineKeyboardRow(row1));
        rows.add(new InlineKeyboardRow(row2));

        return new InlineKeyboardMarkup(rows);
    }
}
