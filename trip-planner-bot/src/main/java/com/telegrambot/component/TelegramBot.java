package com.telegrambot.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.CommandLongPollingTelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.telegrambot.entity.Trip;
import com.telegrambot.service.TripService;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Component
public class TelegramBot extends CommandLongPollingTelegramBot {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    private final ConcurrentHashMap<Integer, Long> usersChats;

    private final ScheduledExecutorService scheduler;

    private static final int BACKGROUND_TASK_PERIOD = 3600;

    private final TripService tripService;

    public TelegramBot(TelegramClient client, @Value("${bot.name}") String botName, TripService tripService) {
        super(client, true, () -> botName);
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::backgroundTasks, 0, BACKGROUND_TASK_PERIOD, TimeUnit.SECONDS);
        usersChats = new ConcurrentHashMap<>();
        this.tripService = tripService;
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
            sendResponse(chatId, "Invalid command: " + messageText +
            " Try /help to see the possible commands");
        }
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            sendResponse(chatId, "I'm just a bot that will help you record the" +
                " details of your trip, I can't reply to messages that aren't commands.\n\n" +
                "If you want to know what I can do, write /help.");
        }

        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            switch (callbackData) {
                case "showTrips:1":
                    sendTrips(chatId, "planned");
                    break;
                case "showTrips:2":
                    sendTrips(chatId, "in-progress");
                    break;
                case "showTrips:3":
                    sendTrips(chatId, "finished");
                    break;
                case "showTrips:4":
                    sendTrips(chatId, "all");
                    break;
                default:
                    sendResponse(chatId, "Unknown action: " + callbackData);
                    break;
            }
            return;
        }
    }

    private void sendTrips(Long chatId, String progress) {
        List<Trip> trips = tripService.getTripsByProgress(progress, chatId);
        if (trips.isEmpty()) {
            sendResponse(chatId, "No trips found with progress: " + progress);
        } else {
            StringBuilder response;
            if (progress == "all") {
                response = new StringBuilder("All trips:\n");
            } else {
                response = new StringBuilder("Trips with progress \'" + progress + "\':\n");
            }

            for (Trip trip : trips) {
                response.append("- ").append(trip.getNameTrip()).append("\n");
            }
            sendResponse(chatId, response.toString());
        }
    }

    // Вспомогательный метод для отправки сообщения
    private void sendResponse(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error sending message: " + e.getMessage(), e);
        }
    }
}
