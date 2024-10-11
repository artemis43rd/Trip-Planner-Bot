package com.telegrambot.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.CommandLongPollingTelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import com.telegrambot.entity.Point;
import com.telegrambot.service.PointService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Component
public class TelegramBot extends CommandLongPollingTelegramBot {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    // TODO remove this hack and use db
    private final ConcurrentHashMap<Integer, Long> usersChats;

    private final ScheduledExecutorService scheduler;

    private static final int BACKGROUND_TASK_PERIOD = 120;

    public TelegramBot(TelegramClient client, @Value("${bot.name}") String botName) {
        super(client, true, () -> botName);
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::backgroundTasks, 0, BACKGROUND_TASK_PERIOD, TimeUnit.SECONDS);
        usersChats = new ConcurrentHashMap<>();
    }

    private void backgroundTasks() {
        logger.info("Update background tasks.");
        //checkForDeadlines();
    }

    /*private PointService pointService;

    private void checkForDeadlines() {
        for (var entry : usersChats.entrySet()) {
            Integer telegramId = entry.getKey();
            Long chatId = entry.getValue();
    
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime plusWeek = now.plusWeeks(1);
    
            Timestamp plusDayTimestamp = Timestamp.from(now.toInstant(ZoneOffset.UTC));
            Timestamp plusWeekTimestamp = Timestamp.from(plusWeek.toInstant(ZoneOffset.UTC));
    
            pointService.getByDeadline(telegramId, plusDayTimestamp, plusWeekTimestamp)
                    .forEach(point -> {
                        SendMessage notify = new SendMessage(chatId.toString(), "You should check your points. You have trip in the next week.");
                        try {
                            telegramClient.execute(notify);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }*/

    @Override
    public boolean filter(Message message) {
        logger.info("Got message: " + message.getText() + " from User: " + message.getFrom().getUserName() + " (" + message.getFrom().getId() + ")");
        usersChats.put(message.getFrom().getId().intValue(), message.getChatId());
        return super.filter(message);
    }

    @Override
    public void processInvalidCommandUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            // Сообщение о некорректной команде
            SendMessage message = new SendMessage(chatId.toString(), "Invalid command: " + messageText +
            " Try /help to see the possible commands");

            try {
                telegramClient.execute(message);
            } catch (TelegramApiException e) {
                logger.error("Error sending message: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();

            // Ответ на произвольное сообщение
            SendMessage message = new SendMessage(chatId.toString(), "I'm just a bot that will help you record the" +
            " details of your trip, I can't reply to messages that aren't commands.\n\n" +
            "If you want to know what I can do, write /help.");

            try {
                telegramClient.execute(message);
            } catch (TelegramApiException e) {
                logger.error("Error sending message: " + e.getMessage(), e);
            }
        }
    }
}
